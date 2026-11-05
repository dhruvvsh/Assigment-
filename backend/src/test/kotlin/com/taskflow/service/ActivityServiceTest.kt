package com.taskflow.service

import com.taskflow.domain.Project
import com.taskflow.domain.Task
import com.taskflow.domain.TaskActivity
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.TaskActivityRepository
import com.taskflow.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ActivityServiceTest {

	private val activityRepository = mockk<TaskActivityRepository>()
	private val taskRepository = mockk<TaskRepository>()
	private val service = ActivityService(activityRepository, taskRepository)

	private val user = User(name = "Ann", email = "ann@test.com", passwordHash = "hashed", id = 1L)
	private val task = Task(title = "T", description = null, project = Project("P", null, user, id = 5L), id = 10L)

	@Test
	fun `record saves who changed the status and from what to what`() {
		val saved = slot<TaskActivity>()
		every { activityRepository.save(capture(saved)) } answers { saved.captured }

		service.record(task, user, from = null, to = TaskStatus.TODO)

		assertSame(task, saved.captured.task)
		assertSame(user, saved.captured.changedBy)
		assertNull(saved.captured.fromStatus)
		assertEquals(TaskStatus.TODO, saved.captured.toStatus)
	}

	@Test
	fun `listForTask returns the history for the task's owner`() {
		every { taskRepository.findByIdAndProjectOwnerId(10L, 1L) } returns task
		every { activityRepository.findAllByTaskIdOrderByChangedAtDescIdDesc(10L) } returns listOf(
			TaskActivity(task, user, TaskStatus.IN_PROGRESS, TaskStatus.DONE, id = 2L),
			TaskActivity(task, user, null, TaskStatus.TODO, id = 1L),
		)

		val history = service.listForTask(10L, 1L)

		assertEquals(listOf(TaskStatus.DONE, TaskStatus.TODO), history.map { it.toStatus })
		assertEquals(1L, history.first().changedBy.id)
	}

	@Test
	fun `listForTask does not reveal history of a task the caller does not own`() {
		every { taskRepository.findByIdAndProjectOwnerId(10L, 2L) } returns null

		assertFailsWith<NotFoundException> { service.listForTask(10L, 2L) }
		verify(exactly = 0) { activityRepository.findAllByTaskIdOrderByChangedAtDescIdDesc(any()) }
	}
}
