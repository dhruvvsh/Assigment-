package com.taskflow.service

import com.taskflow.domain.Task
import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.PageResponse
import com.taskflow.dto.ProjectSummaryResponse
import com.taskflow.dto.TaskMapper
import com.taskflow.dto.TaskResponse
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.dto.toPageResponse
import com.taskflow.exception.BadRequestException
import com.taskflow.exception.FieldErrorDetail
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.TaskSpecifications
import com.taskflow.repository.UserRepository
import com.taskflow.sorting.TaskSortResolver
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/** The optional filters accepted when listing a project's tasks. */
data class TaskFilter(
	val status: TaskStatus? = null,
	val priority: TaskPriority? = null,
	val assigneeId: Long? = null,
)

@Service
class TaskService(
	private val taskRepository: TaskRepository,
	private val userRepository: UserRepository,
	private val projectService: ProjectService,
	private val activityService: ActivityService,
	private val sortResolver: TaskSortResolver,
	private val mapper: TaskMapper,
	private val clock: Clock,
) {

	@Transactional(readOnly = true)
	fun list(
		projectId: Long,
		userId: Long,
		filter: TaskFilter,
		sortBy: String,
		direction: String,
		page: Int,
		size: Int,
	): PageResponse<TaskResponse> {
		projectService.getOwnedProject(projectId, userId)

		val spec = Specification.allOf(
			listOfNotNull(
				TaskSpecifications.forProject(projectId),
				TaskSpecifications.hasStatus(filter.status),
				TaskSpecifications.hasPriority(filter.priority),
				TaskSpecifications.hasAssignee(filter.assigneeId),
			),
		)
		val sort = sortResolver.resolve(sortBy, parseDirection(direction))
		val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, MAX_PAGE_SIZE), sort)

		return taskRepository.findAll(spec, pageable).toPageResponse(mapper::toResponse)
	}

	@Transactional
	fun create(projectId: Long, userId: Long, request: CreateTaskRequest): TaskResponse {
		val project = projectService.getOwnedProject(projectId, userId)
		val task = taskRepository.save(
			Task(
				title = request.title.trim(),
				description = request.description.cleaned(),
				priority = request.priority,
				dueDate = request.dueDate,
				assignee = resolveAssignee(request.assigneeId),
				project = project,
			),
		)
		activityService.record(task, userRepository.getReferenceById(userId), from = null, to = task.status)
		return mapper.toResponse(task)
	}

	@Transactional
	fun update(taskId: Long, userId: Long, request: UpdateTaskRequest): TaskResponse {
		val task = getOwnedTask(taskId, userId)
		task.title = request.title.trim()
		task.description = request.description.cleaned()
		task.priority = requireNotNull(request.priority)
		task.dueDate = request.dueDate
		task.assignee = resolveAssignee(request.assigneeId)
		changeStatus(task, requireNotNull(request.status), userId)
		return mapper.toResponse(task)
	}

	@Transactional
	fun updateStatus(taskId: Long, userId: Long, status: TaskStatus): TaskResponse {
		val task = getOwnedTask(taskId, userId)
		changeStatus(task, status, userId)
		return mapper.toResponse(task)
	}

	@Transactional
	fun delete(taskId: Long, userId: Long) {
		taskRepository.delete(getOwnedTask(taskId, userId))
	}

	@Transactional(readOnly = true)
	fun summary(projectId: Long, userId: Long): ProjectSummaryResponse {
		projectService.getOwnedProject(projectId, userId)

		val counts = taskRepository.countByStatus(projectId).associate { it.status to it.total }
		// Every status is always present, even with zero tasks, so clients need no null checks.
		val byStatus = TaskStatus.entries.associateWith { counts[it] ?: 0L }
		val overdue = taskRepository.countByProjectIdAndStatusNotAndDueDateBefore(
			projectId, TaskStatus.DONE, LocalDate.now(clock),
		)
		return ProjectSummaryResponse(total = byStatus.values.sum(), byStatus = byStatus, overdue = overdue)
	}

	/** The single place a status changes, so every change (PUT or PATCH) lands in the activity log. */
	private fun changeStatus(task: Task, newStatus: TaskStatus, userId: Long) {
		val previous = task.status
		if (previous == newStatus) return
		task.status = newStatus
		activityService.record(task, userRepository.getReferenceById(userId), from = previous, to = newStatus)
	}

	private fun getOwnedTask(taskId: Long, userId: Long): Task =
		taskRepository.findByIdAndProjectOwnerId(taskId, userId)
			?: throw NotFoundException("Task $taskId not found")

	private fun resolveAssignee(assigneeId: Long?): User? = assigneeId?.let { id ->
		userRepository.findById(id).orElseThrow {
			BadRequestException(
				"Validation failed",
				listOf(FieldErrorDetail("assigneeId", "Assignee $id does not exist")),
			)
		}
	}

	private fun parseDirection(direction: String): Sort.Direction =
		Sort.Direction.fromOptionalString(direction).orElseThrow {
			BadRequestException("Unsupported direction '$direction'. Allowed values: asc, desc")
		}

	private fun String?.cleaned(): String? = this?.trim()?.ifBlank { null }

	private companion object {
		const val MAX_PAGE_SIZE = 100
	}
}
