package com.taskflow

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.util.UUID

/** Registers a fresh user (unique email per call) and returns their JWT. */
fun registerAndGetToken(mockMvc: MockMvc, objectMapper: ObjectMapper, name: String = "Test User"): String {
	val email = "${UUID.randomUUID()}@test.com"
	val body = mockMvc.perform(
		post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""{"name":"$name","email":"$email","password":"Password123!"}"""),
	).andReturn().response.contentAsString
	return objectMapper.readTree(body).get("token").asText()
}
