package com.taskflow.service

import com.taskflow.domain.User
import com.taskflow.dto.LoginRequest
import com.taskflow.dto.RegisterRequest
import com.taskflow.exception.ConflictException
import com.taskflow.exception.UnauthorizedException
import com.taskflow.repository.UserRepository
import com.taskflow.security.JwtService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class AuthServiceTest {

	private val userRepository = mockk<UserRepository>()
	private val passwordEncoder = mockk<PasswordEncoder>()
	private val jwtService = mockk<JwtService>()
	private val service = AuthService(userRepository, passwordEncoder, jwtService)

	private fun storedUser(email: String = "ann@test.com") =
		User(name = "Ann", email = email, passwordHash = "hashed", id = 1L)

	@Test
	fun `register normalizes the email, hashes the password and returns a token`() {
		val saved = slot<User>()
		every { userRepository.existsByEmail("ann@test.com") } returns false
		every { passwordEncoder.encode("Password123!") } returns "hashed"
		every { userRepository.save(capture(saved)) } answers { storedUser() }
		every { jwtService.generateToken(any()) } returns "jwt-token"

		val response = service.register(RegisterRequest("  Ann ", "  Ann@Test.COM ", "Password123!"))

		assertEquals("ann@test.com", saved.captured.email)
		assertEquals("Ann", saved.captured.name)
		assertEquals("hashed", saved.captured.passwordHash)
		assertNotEquals("Password123!", saved.captured.passwordHash)
		assertEquals("jwt-token", response.token)
		assertEquals(1L, response.user.id)
	}

	@Test
	fun `register rejects an email that is already taken`() {
		every { userRepository.existsByEmail("ann@test.com") } returns true

		assertFailsWith<ConflictException> {
			service.register(RegisterRequest("Ann", "ann@test.com", "Password123!"))
		}
		verify(exactly = 0) { userRepository.save(any()) }
	}

	@Test
	fun `login returns a token for correct credentials`() {
		every { userRepository.findByEmail("ann@test.com") } returns storedUser()
		every { passwordEncoder.matches("Password123!", "hashed") } returns true
		every { jwtService.generateToken(any()) } returns "jwt-token"

		val response = service.login(LoginRequest("ANN@test.com", "Password123!"))

		assertEquals("jwt-token", response.token)
	}

	@Test
	fun `login gives the same error for a wrong password and an unknown email`() {
		every { userRepository.findByEmail("ann@test.com") } returns storedUser()
		every { passwordEncoder.matches("wrong", "hashed") } returns false
		every { userRepository.findByEmail("nobody@test.com") } returns null

		val wrongPassword = assertFailsWith<UnauthorizedException> { service.login(LoginRequest("ann@test.com", "wrong")) }
		val unknownEmail = assertFailsWith<UnauthorizedException> { service.login(LoginRequest("nobody@test.com", "x")) }

		assertEquals(wrongPassword.message, unknownEmail.message)
	}
}
