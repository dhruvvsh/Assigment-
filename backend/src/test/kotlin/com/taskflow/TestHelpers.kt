package com.taskflow

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.util.UUID

data class TestUser(val token: String, val id: Long)

/** Registers a fresh user (unique email per call) and returns their id and JWT. */
fun registerUser(mockMvc: MockMvc, objectMapper: ObjectMapper, name: String = "Test User"): TestUser {
	val email = "${UUID.randomUUID()}@test.com"
	val body = mockMvc.perform(
		post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""{"name":"$name","email":"$email","password":"Password123!"}"""),
	).andReturn().response.contentAsString
	val json = objectMapper.readTree(body)
	return TestUser(json.get("token").asText(), json.get("user").get("id").asLong())
}

fun registerAndGetToken(mockMvc: MockMvc, objectMapper: ObjectMapper, name: String = "Test User"): String =
	registerUser(mockMvc, objectMapper, name).token
