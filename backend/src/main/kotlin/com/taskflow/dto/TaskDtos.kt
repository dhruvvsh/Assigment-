package com.taskflow.dto

import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import java.time.Instant
import java.time.LocalDate

data class CreateTaskRequest(
	@field:NotBlank(message = "Title is required")
	@field:Size(max = 120, message = "Title must be at most 120 characters")
	val title: String,

	@field:Size(max = 2000, message = "Description must be at most 2000 characters")
	val description: String? = null,

	val priority: TaskPriority = TaskPriority.MEDIUM,

	// Only creation rejects past dates; an existing task may legitimately become overdue.
	@field:FutureOrPresent(message = "Due date cannot be in the past")
	val dueDate: LocalDate? = null,

	val assigneeId: Long? = null,
)

/** PUT replaces every editable field, so an omitted assigneeId or dueDate clears it. */
data class UpdateTaskRequest(
	@field:NotBlank(message = "Title is required")
	@field:Size(max = 120, message = "Title must be at most 120 characters")
	val title: String,

	@field:Size(max = 2000, message = "Description must be at most 2000 characters")
	val description: String? = null,

	@field:NotNull(message = "Status is required")
	val status: TaskStatus?,

	@field:NotNull(message = "Priority is required")
	val priority: TaskPriority?,

	val dueDate: LocalDate? = null,

	val assigneeId: Long? = null,
)

data class UpdateStatusRequest(
	@field:NotNull(message = "Status is required")
	val status: TaskStatus?,
)

data class TaskResponse(
	val id: Long,
	val title: String,
	val description: String?,
	val status: TaskStatus,
	val priority: TaskPriority,
	val dueDate: LocalDate?,
	val overdue: Boolean,
	val assignee: UserResponse?,
	val projectId: Long,
	val createdAt: Instant,
	val updatedAt: Instant,
)

data class ProjectSummaryResponse(
	val total: Long,
	val byStatus: Map<TaskStatus, Long>,
	val overdue: Long,
)

/** Our own page shape, so the API contract does not depend on Spring's Page serialisation. */
data class PageResponse<T>(
	val content: List<T>,
	val page: Int,
	val size: Int,
	val totalElements: Long,
	val totalPages: Int,
)

fun <T, R> Page<T>.toPageResponse(transform: (T) -> R) = PageResponse(
	content = content.map(transform),
	page = number,
	size = size,
	totalElements = totalElements,
	totalPages = totalPages,
)
