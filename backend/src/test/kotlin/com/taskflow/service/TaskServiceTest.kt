package com.taskflow.service

import com.taskflow.domain.Project
import com.taskflow.domain.Task
import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.TaskMapper
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.exception.BadRequestException
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.StatusCount
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import com.taskflow.sorting.DueDateSortStrategy
import com.taskflow.sorting.PrioritySortStrategy
import com.taskflow.sorting.TaskSortResolver
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskServiceTest {

	private val taskRepository = mockk<TaskRepository>()
	private val userRepository = mockk<UserRepository>()
	private val projectService = mockk<ProjectService>()
	private val activityService = mockk<ActivityService>()
	private val clock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneOffset.UTC)
	private val today = LocalDate.of(2026, 11, 10)

	private val service = TaskService(
		taskRepository,
		userRepository,
		projectService,
		activityService,
		TaskSortResolver(listOf(DueDateSortStrategy(), PrioritySortStrategy())),
		TaskMapper(clock),
		clock,
	)

	private val user = User(name = "Ann", email = "ann@test.com", passwordHash = "hashed", id = 1L)
	private val project = Project("Board", null, user, id = 5L)

	private fun existingTask(status: TaskStatus = TaskStatus.TODO) = Task(
		title = "Existing",
		description = null,
		status = status,
		priority = TaskPriority.MEDIUM,
		project = project,
		id = 10L,
	)

	private fun updateRequest(status: TaskStatus, assigneeId: Long? = null) = UpdateTaskRequest(
		title = "  Renamed  ",
		description = "details",
		status = status,
		priority = TaskPriority.HIGH,
		dueDate = today.minusDays(1),
		assigneeId = assigneeId,
	)

	private fun allowActivityLogging() {
		every { userRepository.getReferenceById(1L) } returns user
		every { activityService.record(any(), any(), any(), any()) } just Runs
	}

	@Test
	fun `create trims input, starts in TODO and logs the creation`() {
		val project5 = project
		every { projectService.getOwnedProject(5L, 1L) } returns project5
		every { taskRepository.save(any()) } answers {
			val t = firstArg<Task>()
			Task(t.title, t.description, t.status, t.priority, t.dueDate, t.assignee, t.project, id = 10L)
		}
		allowActivityLogging()

		val response = service.create(
			5L, 1L, CreateTaskRequest("  Write docs  ", "  ", TaskPriority.HIGH, today.plusDays(2)),
		)

		assertEquals("Write docs", response.title)
		assertNull(response.description)
		assertEquals(TaskStatus.TODO, response.status)
		assertEquals(TaskPriority.HIGH, response.priority)
		verify { activityService.record(match { it.id == 10L }, user, null, TaskStatus.TODO) }
	}

	@Test
	fun `create fails when the project is not owned by the caller`() {
		every { projectService.getOwnedProject(5L, 2L) } throws NotFoundException("Project 5 not found")

		assertFailsWith<NotFoundException> { service.create(5L, 2L, CreateTaskRequest("Title")) }
		verify(exactly = 0) { taskRepository.save(any()) }
	}

	@Test
	fun `create rejects an assignee that does not exist with a field error`() {
		every { projectService.getOwnedProject(5L, 1L) } returns project
		every { userRepository.findById(77L) } returns Optional.empty()

		val error = assertFailsWith<BadRequestException> {
			service.create(5L, 1L, CreateTaskRequest("Title", assigneeId = 77L))
		}

		assertEquals("assigneeId", error.fieldErrors.single().field)
	}

	@Test
	fun `update changes the fields, allows a past due date and logs the status change`() {
		val task = existingTask(TaskStatus.TODO)
		every { taskRepository.findByIdAndProjectOwnerId(10L, 1L) } returns task
		allowActivityLogging()

		val response = service.update(10L, 1L, updateRequest(TaskStatus.IN_PROGRESS))

		assertEquals("Renamed", task.title)
		assertEquals(TaskPriority.HIGH, task.priority)
		assertEquals(today.minusDays(1), task.dueDate)
		assertTrue(response.overdue)
		verify { activityService.record(task, user, TaskStatus.TODO, TaskStatus.IN_PROGRESS) }
	}

	@Test
	fun `update does not log activity when the status is unchanged`() {
		val task = existingTask(TaskStatus.DONE)
		every { taskRepository.findByIdAndProjectOwnerId(10L, 1L) } returns task
		allowActivityLogging()

		service.update(10L, 1L, updateRequest(TaskStatus.DONE))

		verify(exactly = 0) { activityService.record(any(), any(), any(), any()) }
	}

	@Test
	fun `update of a task the caller does not own is reported as not found`() {
		every { taskRepository.findByIdAndProjectOwnerId(10L, 2L) } returns null

		assertFailsWith<NotFoundException> { service.update(10L, 2L, updateRequest(TaskStatus.DONE)) }
	}

	@Test
	fun `updateStatus moves the task and logs who did it`() {
		val task = existingTask(TaskStatus.TODO)
		every { taskRepository.findByIdAndProjectOwnerId(10L, 1L) } returns task
		allowActivityLogging()

		val response = service.updateStatus(10L, 1L, TaskStatus.DONE)

		assertEquals(TaskStatus.DONE, response.status)
		verify(exactly = 1) { activityService.record(task, user, TaskStatus.TODO, TaskStatus.DONE) }
	}

	@Test
	fun `delete removes an owned task`() {
		val task = existingTask()
		every { taskRepository.findByIdAndProjectOwnerId(10L, 1L) } returns task
		every { taskRepository.delete(task) } just Runs

		service.delete(10L, 1L)

		verify { taskRepository.delete(task) }
	}

	@Test
	fun `summary reports zero for statuses with no tasks and the overdue count`() {
		every { projectService.getOwnedProject(5L, 1L) } returns project
		every { taskRepository.countByStatus(5L) } returns listOf(
			object : StatusCount {
				override val status = TaskStatus.TODO
				override val total = 3L
			},
			object : StatusCount {
				override val status = TaskStatus.DONE
				override val total = 1L
			},
		)
		every {
			taskRepository.countByProjectIdAndStatusNotAndDueDateBefore(5L, TaskStatus.DONE, today)
		} returns 2L

		val summary = service.summary(5L, 1L)

		assertEquals(4L, summary.total)
		assertEquals(3L, summary.byStatus[TaskStatus.TODO])
		assertEquals(0L, summary.byStatus[TaskStatus.IN_PROGRESS])
		assertEquals(1L, summary.byStatus[TaskStatus.DONE])
		assertEquals(2L, summary.overdue)
	}

	@Test
	fun `list caps the page size and applies the requested sort`() {
		val pageable = slot<Pageable>()
		every { projectService.getOwnedProject(5L, 1L) } returns project
		every { taskRepository.findAll(any<Specification<Task>>(), capture(pageable)) } returns
			PageImpl(listOf(existingTask()))

		val page = service.list(5L, 1L, TaskFilter(), "priority", "desc", page = -3, size = 500)

		assertEquals(1, page.content.size)
		assertEquals(100, pageable.captured.pageSize)
		assertEquals(0, pageable.captured.pageNumber)
		assertEquals(Sort.Direction.DESC, pageable.captured.sort.getOrderFor("priority")?.direction)
	}

	@Test
	fun `list rejects an unsupported sort direction`() {
		every { projectService.getOwnedProject(5L, 1L) } returns project

		assertFailsWith<BadRequestException> {
			service.list(5L, 1L, TaskFilter(), "dueDate", "sideways", page = 0, size = 20)
		}
	}
}
