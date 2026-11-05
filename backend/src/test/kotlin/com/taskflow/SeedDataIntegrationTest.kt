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
import kotlin.test.assertEquals

@SpringBootTest(properties = ["app.seed.enabled=true"])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class SeedDataIntegrationTest(
	@Autowired private val mockMvc: MockMvc,
	@Autowired private val objectMapper: ObjectMapper,
) {

	@Test
	fun `the demo user can log in and sees a populated board`() {
		val login = mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"email":"demo@taskflow.com","password":"Password123!"}"""),
		).andExpect(status().isOk).andReturn().response.contentAsString
		val token = objectMapper.readTree(login).get("token").asText()

		val projects = mockMvc.perform(get("/api/projects").header("Authorization", "Bearer $token"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(2))
			.andReturn().response.contentAsString
		val website = objectMapper.readTree(projects).first { it.get("name").asText() == "Website Redesign" }
		val projectId = website.get("id").asLong()
		assertEquals("Demo User", website.get("owner").get("name").asText())

		mockMvc.perform(get("/api/projects/$projectId/summary").header("Authorization", "Bearer $token"))
			.andExpect(jsonPath("$.total").value(6))
			.andExpect(jsonPath("$.byStatus.TODO").value(3))
			.andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(2))
			.andExpect(jsonPath("$.byStatus.DONE").value(1))
			.andExpect(jsonPath("$.overdue").value(1))
	}
}
