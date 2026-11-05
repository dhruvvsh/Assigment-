package com.taskflow.dto

import com.taskflow.domain.Task
import com.taskflow.domain.TaskStatus
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate

/**
 * Builds [TaskResponse] objects from entities. It lives in one place so the derived
 * `overdue` rule (past due date and not done) is defined exactly once.
 */
@Component
class TaskMapper(private val clock: Clock) {

	fun toResponse(task: Task): TaskResponse {
		val dueDate = task.dueDate
		return TaskResponse(
			id = requireNotNull(task.id) { "Task must be persisted before mapping" },
			title = task.title,
			description = task.description,
			status = task.status,
			priority = task.priority,
			dueDate = dueDate,
			overdue = dueDate != null && dueDate.isBefore(LocalDate.now(clock)) && task.status != TaskStatus.DONE,
			assignee = task.assignee?.toResponse(),
			projectId = requireNotNull(task.project.id),
			createdAt = task.createdAt,
			updatedAt = task.updatedAt,
		)
	}
}
