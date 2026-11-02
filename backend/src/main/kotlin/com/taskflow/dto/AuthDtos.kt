package com.taskflow.dto

import com.taskflow.domain.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class RegisterRequest(
	@field:NotBlank(message = "Name is required")
	@field:Size(max = 100, message = "Name must be at most 100 characters")
	val name: String,

	@field:NotBlank(message = "Email is required")
	@field:Email(message = "Email must be a valid email address")
	@field:Size(max = 255, message = "Email must be at most 255 characters")
	val email: String,

	@field:NotBlank(message = "Password is required")
	@field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
	val password: String,
)

data class LoginRequest(
	@field:NotBlank(message = "Email is required")
	val email: String,

	@field:NotBlank(message = "Password is required")
	val password: String,
)

data class UserResponse(
	val id: Long,
	val name: String,
	val email: String,
	val createdAt: Instant,
)

data class AuthResponse(
	val token: String,
	val user: UserResponse,
)

fun User.toResponse() = UserResponse(
	id = requireNotNull(id) { "User must be persisted before mapping" },
	name = name,
	email = email,
	createdAt = createdAt,
)
