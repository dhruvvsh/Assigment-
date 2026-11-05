package com.taskflow.dto

import com.taskflow.domain.Project
import com.taskflow.domain.Task
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskMapperTest {

	private val clock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneOffset.UTC)
	private val today = LocalDate.of(2026, 11, 10)
	private val mapper = TaskMapper(clock)
	private val owner = User(name = "Ann", email = "ann@test.com", passwordHash = "hashed", id = 1L)
	private val project = Project("P", null, owner, id = 5L)

	private fun task(dueDate: LocalDate?, status: TaskStatus, assignee: User? = null) = Task(
		title = "T", description = null, status = status, dueDate = dueDate,
		assignee = assignee, project = project, id = 10L,
	)

	@Test
	fun `a task due before today and not done is overdue`() {
		assertTrue(mapper.toResponse(task(today.minusDays(1), TaskStatus.TODO)).overdue)
		assertTrue(mapper.toResponse(task(today.minusDays(1), TaskStatus.IN_PROGRESS)).overdue)
	}

	@Test
	fun `a done task is never overdue`() {
		assertFalse(mapper.toResponse(task(today.minusDays(30), TaskStatus.DONE)).overdue)
	}

	@Test
	fun `a task due today or later, or without a due date, is not overdue`() {
		assertFalse(mapper.toResponse(task(today, TaskStatus.TODO)).overdue)
		assertFalse(mapper.toResponse(task(today.plusDays(1), TaskStatus.TODO)).overdue)
		assertFalse(mapper.toResponse(task(null, TaskStatus.TODO)).overdue)
	}

	@Test
	fun `the response exposes the assignee without sensitive fields`() {
		val response = mapper.toResponse(task(null, TaskStatus.TODO, assignee = owner))

		assertEquals(1L, response.assignee?.id)
		assertEquals(5L, response.projectId)
		assertNull(mapper.toResponse(task(null, TaskStatus.TODO)).assignee)
	}
}
