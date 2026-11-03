package com.taskflow.controller

import com.taskflow.dto.CreateProjectRequest
import com.taskflow.dto.ProjectResponse
import com.taskflow.security.AuthUser
import com.taskflow.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects")
class ProjectController(private val projectService: ProjectService) {

	@GetMapping
	fun list(@AuthenticationPrincipal user: AuthUser): List<ProjectResponse> =
		projectService.listForUser(user.id)

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(
		@AuthenticationPrincipal user: AuthUser,
		@Valid @RequestBody request: CreateProjectRequest,
	): ProjectResponse = projectService.create(user.id, request)
}
