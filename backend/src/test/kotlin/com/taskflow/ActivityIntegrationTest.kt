package com.taskflow

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ActivityIntegrationTest(
	@Autowired private val mockMvc: MockMvc,
	@Autowired private val objectMapper: ObjectMapper,
) {

	private fun send(token: String, builder: MockHttpServletRequestBuilder, body: String? = null): ResultActions {
		builder.header("Authorization", "Bearer $token")
		if (body != null) builder.contentType(MediaType.APPLICATION_JSON).content(body)
		return mockMvc.perform(builder)
	}

	private fun idOf(result: ResultActions): Long =
		objectMapper.readTree(result.andReturn().response.contentAsString).get("id").asLong()

	@Test
	fun `status changes are logged with who made them, newest first`() {
		val user = registerUser(mockMvc, objectMapper, "Mover")
		val projectId = idOf(send(user.token, post("/api/projects"), """{"name":"P"}"""))
		val taskId = idOf(send(user.token, post("/api/projects/$projectId/tasks"), """{"title":"Task"}"""))

		send(user.token, patch("/api/tasks/$taskId/status"), """{"status":"IN_PROGRESS"}""")
		send(user.token, put("/api/tasks/$taskId"), """{"title":"Task","status":"DONE","priority":"MEDIUM"}""")
		// Same status again: nothing new should be logged.
		send(user.token, put("/api/tasks/$taskId"), """{"title":"Renamed","status":"DONE","priority":"MEDIUM"}""")

		send(user.token, get("/api/tasks/$taskId/activity"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(3))
			.andExpect(jsonPath("$[0].fromStatus").value("IN_PROGRESS"))
			.andExpect(jsonPath("$[0].toStatus").value("DONE"))
			.andExpect(jsonPath("$[0].changedBy.id").value(user.id))
			.andExpect(jsonPath("$[0].changedBy.name").value("Mover"))
			.andExpect(jsonPath("$[0].changedAt").isNotEmpty)
			.andExpect(jsonPath("$[2].fromStatus").doesNotExist())
			.andExpect(jsonPath("$[2].toStatus").value("TODO"))
	}

	@Test
	fun `only the project owner can read a task's history`() {
		val alice = registerAndGetToken(mockMvc, objectMapper)
		val bob = registerAndGetToken(mockMvc, objectMapper)
		val projectId = idOf(send(alice, post("/api/projects"), """{"name":"P"}"""))
		val taskId = idOf(send(alice, post("/api/projects/$projectId/tasks"), """{"title":"Task"}"""))

		send(bob, get("/api/tasks/$taskId/activity")).andExpect(status().isNotFound)
	}

	@Test
	fun `deleting a task also removes its history`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = idOf(send(token, post("/api/projects"), """{"name":"P"}"""))
		val taskId = idOf(send(token, post("/api/projects/$projectId/tasks"), """{"title":"Task"}"""))
		send(token, patch("/api/tasks/$taskId/status"), """{"status":"DONE"}""")

		send(token, delete("/api/tasks/$taskId")).andExpect(status().isNoContent)
		send(token, get("/api/tasks/$taskId/activity")).andExpect(status().isNotFound)
	}
}
