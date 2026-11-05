package com.taskflow.controller

import com.taskflow.dto.UserResponse
import com.taskflow.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

	@GetMapping
	fun list(): List<UserResponse> = userService.listAll()
}
