package com.taskflow

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ProjectIntegrationTest(
	@Autowired private val mockMvc: MockMvc,
	@Autowired private val objectMapper: ObjectMapper,
) {

	private fun createProject(token: String, body: String) = mockMvc.perform(
		post("/api/projects")
			.header("Authorization", "Bearer $token")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body),
	)

	private fun listProjects(token: String) =
		mockMvc.perform(get("/api/projects").header("Authorization", "Bearer $token"))

	@Test
	fun `creating a project returns it and lists it for the owner`() {
		val token = registerAndGetToken(mockMvc, objectMapper, "Owner")

		createProject(token, """{"name":"  Website  ","description":"Redesign"}""")
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.name").value("Website"))
			.andExpect(jsonPath("$.owner.name").value("Owner"))
			.andExpect(jsonPath("$.owner.passwordHash").doesNotExist())

		listProjects(token)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].description").value("Redesign"))
	}

	@Test
	fun `users only see their own projects`() {
		val alice = registerAndGetToken(mockMvc, objectMapper)
		val bob = registerAndGetToken(mockMvc, objectMapper)
		createProject(alice, """{"name":"Alice's project"}""").andExpect(status().isCreated)

		listProjects(bob)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(0))
	}

	@Test
	fun `project name is required and limited to 100 characters`() {
		val token = registerAndGetToken(mockMvc, objectMapper)

		createProject(token, """{"name":""}""")
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"))

		createProject(token, """{"name":"${"x".repeat(101)}"}""")
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].message").value("Name must be at most 100 characters"))
	}

	@Test
	fun `projects endpoints require authentication`() {
		mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized)
		mockMvc.perform(
			post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""{"name":"x"}"""),
		).andExpect(status().isUnauthorized)
	}
}
