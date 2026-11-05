package com.taskflow.controller

import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.PageResponse
import com.taskflow.dto.ProjectSummaryResponse
import com.taskflow.dto.TaskResponse
import com.taskflow.dto.UpdateStatusRequest
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.security.AuthUser
import com.taskflow.service.TaskFilter
import com.taskflow.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskController(private val taskService: TaskService) {

	@GetMapping("/api/projects/{projectId}/tasks")
	fun list(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable projectId: Long,
		@RequestParam(required = false) status: TaskStatus?,
		@RequestParam(required = false) priority: TaskPriority?,
		@RequestParam(required = false) assigneeId: Long?,
		@RequestParam(defaultValue = "dueDate") sortBy: String,
		@RequestParam(defaultValue = "asc") direction: String,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int,
	): PageResponse<TaskResponse> = taskService.list(
		projectId, user.id, TaskFilter(status, priority, assigneeId), sortBy, direction, page, size,
	)

	@PostMapping("/api/projects/{projectId}/tasks")
	@ResponseStatus(HttpStatus.CREATED)
	fun create(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable projectId: Long,
		@Valid @RequestBody request: CreateTaskRequest,
	): TaskResponse = taskService.create(projectId, user.id, request)

	@PutMapping("/api/tasks/{taskId}")
	fun update(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable taskId: Long,
		@Valid @RequestBody request: UpdateTaskRequest,
	): TaskResponse = taskService.update(taskId, user.id, request)

	@PatchMapping("/api/tasks/{taskId}/status")
	fun updateStatus(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable taskId: Long,
		@Valid @RequestBody request: UpdateStatusRequest,
	): TaskResponse = taskService.updateStatus(taskId, user.id, requireNotNull(request.status))

	@DeleteMapping("/api/tasks/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@AuthenticationPrincipal user: AuthUser, @PathVariable taskId: Long) =
		taskService.delete(taskId, user.id)

	@GetMapping("/api/projects/{projectId}/summary")
	fun summary(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable projectId: Long,
	): ProjectSummaryResponse = taskService.summary(projectId, user.id)
}
