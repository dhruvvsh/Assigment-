package com.taskflow.service

import com.taskflow.domain.User
import com.taskflow.dto.AuthResponse
import com.taskflow.dto.LoginRequest
import com.taskflow.dto.RegisterRequest
import com.taskflow.dto.toResponse
import com.taskflow.exception.ConflictException
import com.taskflow.exception.UnauthorizedException
import com.taskflow.repository.UserRepository
import com.taskflow.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService,
) {

	@Transactional
	fun register(request: RegisterRequest): AuthResponse {
		val email = normalize(request.email)
		if (userRepository.existsByEmail(email)) {
			throw ConflictException("An account with this email already exists")
		}
		val user = userRepository.save(
			User(
				name = request.name.trim(),
				email = email,
				passwordHash = passwordEncoder.encode(request.password),
			),
		)
		return AuthResponse(jwtService.generateToken(user), user.toResponse())
	}

	@Transactional(readOnly = true)
	fun login(request: LoginRequest): AuthResponse {
		val user = userRepository.findByEmail(normalize(request.email))
		// One message for both "unknown email" and "wrong password" so accounts cannot be probed.
		if (user == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
			throw UnauthorizedException("Invalid email or password")
		}
		return AuthResponse(jwtService.generateToken(user), user.toResponse())
	}

	private fun normalize(email: String) = email.trim().lowercase()
}
