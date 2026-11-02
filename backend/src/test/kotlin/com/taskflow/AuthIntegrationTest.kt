package com.taskflow

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest(@Autowired private val mockMvc: MockMvc) {

	private fun json(path: String, body: String) = mockMvc.perform(
		post(path).contentType(MediaType.APPLICATION_JSON).content(body),
	)

	@Test
	fun `register returns a token and the user`() {
		json("/api/auth/register", """{"name":"Ann","email":"Ann@Test.com","password":"Password123!"}""")
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.token").isNotEmpty)
			.andExpect(jsonPath("$.user.email").value("ann@test.com"))
			.andExpect(jsonPath("$.user.passwordHash").doesNotExist())
	}

	@Test
	fun `register rejects a duplicate email with 409`() {
		val body = """{"name":"Bob","email":"bob@test.com","password":"Password123!"}"""
		json("/api/auth/register", body).andExpect(status().isCreated)
		json("/api/auth/register", body)
			.andExpect(status().isConflict)
			.andExpect(jsonPath("$.status").value(409))
	}

	@Test
	fun `register validation errors are reported per field`() {
		json("/api/auth/register", """{"name":"","email":"not-an-email","password":"short"}""")
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.fieldErrors.length()").value(3))
	}

	@Test
	fun `login succeeds with correct credentials and fails with wrong ones`() {
		json("/api/auth/register", """{"name":"Cy","email":"cy@test.com","password":"Password123!"}""")
			.andExpect(status().isCreated)

		json("/api/auth/login", """{"email":"cy@test.com","password":"Password123!"}""")
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.token").isNotEmpty)

		json("/api/auth/login", """{"email":"cy@test.com","password":"wrong-password"}""")
			.andExpect(status().isUnauthorized)
			.andExpect(jsonPath("$.message").value("Invalid email or password"))
	}

	@Test
	fun `protected endpoints return 401 without a valid token`() {
		mockMvc.perform(get("/api/projects"))
			.andExpect(status().isUnauthorized)
			.andExpect(jsonPath("$.status").value(401))

		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer not.a.token"))
			.andExpect(status().isUnauthorized)
	}
}
