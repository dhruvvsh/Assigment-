package com.taskflow.controller

import com.taskflow.dto.ActivityResponse
import com.taskflow.security.AuthUser
import com.taskflow.service.ActivityService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(private val activityService: ActivityService) {

	@GetMapping("/api/tasks/{taskId}/activity")
	fun list(
		@AuthenticationPrincipal user: AuthUser,
		@PathVariable taskId: Long,
	): List<ActivityResponse> = activityService.listForTask(taskId, user.id)
}
