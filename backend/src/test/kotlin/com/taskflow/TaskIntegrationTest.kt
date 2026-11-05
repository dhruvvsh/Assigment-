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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class TaskIntegrationTest(
	@Autowired private val mockMvc: MockMvc,
	@Autowired private val objectMapper: ObjectMapper,
) {

	private val today = LocalDate.now()

	private fun send(token: String, builder: org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder, body: String? = null): ResultActions {
		builder.header("Authorization", "Bearer $token")
		if (body != null) builder.contentType(MediaType.APPLICATION_JSON).content(body)
		return mockMvc.perform(builder)
	}

	private fun createProjectId(token: String): Long {
		val response = send(token, post("/api/projects"), """{"name":"Board"}""").andReturn().response.contentAsString
		return objectMapper.readTree(response).get("id").asLong()
	}

	private fun createTask(token: String, projectId: Long, body: String) =
		send(token, post("/api/projects/$projectId/tasks"), body)

	private fun createTaskId(token: String, projectId: Long, body: String): Long =
		objectMapper.readTree(createTask(token, projectId, body).andReturn().response.contentAsString).get("id").asLong()

	private fun task(title: String, priority: String = "MEDIUM", dueInDays: Long? = null, assigneeId: Long? = null): String {
		val due = dueInDays?.let { "\"${today.plusDays(it)}\"" } ?: "null"
		return """{"title":"$title","priority":"$priority","dueDate":$due,"assigneeId":${assigneeId ?: "null"}}"""
	}

	@Test
	fun `creating a task returns it with defaults`() {
		val user = registerUser(mockMvc, objectMapper)
		val projectId = createProjectId(user.token)

		createTask(user.token, projectId, task("Write docs", "HIGH", dueInDays = 2, assigneeId = user.id))
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.title").value("Write docs"))
			.andExpect(jsonPath("$.status").value("TODO"))
			.andExpect(jsonPath("$.priority").value("HIGH"))
			.andExpect(jsonPath("$.overdue").value(false))
			.andExpect(jsonPath("$.assignee.id").value(user.id))
			.andExpect(jsonPath("$.projectId").value(projectId))
	}

	@Test
	fun `task validation reports field errors`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(token)

		createTask(token, projectId, """{"title":""}""")
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].field").value("title"))

		createTask(token, projectId, """{"title":"${"x".repeat(121)}"}""")
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].message").value("Title must be at most 120 characters"))

		createTask(token, projectId, task("Late", dueInDays = -1))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].field").value("dueDate"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("Due date cannot be in the past"))

		createTask(token, projectId, task("Ghost assignee", assigneeId = 999_999))
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.fieldErrors[0].field").value("assigneeId"))
	}

	@Test
	fun `tasks can be filtered by status priority and assignee`() {
		val user = registerUser(mockMvc, objectMapper)
		val projectId = createProjectId(user.token)
		createTask(user.token, projectId, task("A", "HIGH", assigneeId = user.id))
		createTask(user.token, projectId, task("B", "LOW"))
		val c = createTaskId(user.token, projectId, task("C", "HIGH"))
		send(user.token, patch("/api/tasks/$c/status"), """{"status":"IN_PROGRESS"}""").andExpect(status().isOk)

		send(user.token, get("/api/projects/$projectId/tasks?priority=HIGH"))
			.andExpect(jsonPath("$.totalElements").value(2))
		send(user.token, get("/api/projects/$projectId/tasks?priority=HIGH&status=IN_PROGRESS"))
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].title").value("C"))
		send(user.token, get("/api/projects/$projectId/tasks?assigneeId=${user.id}"))
			.andExpect(jsonPath("$.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].title").value("A"))
	}

	@Test
	fun `tasks can be sorted by due date and by priority`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(token)
		createTask(token, projectId, task("Soon-low", "LOW", dueInDays = 1))
		createTask(token, projectId, task("Later-high", "HIGH", dueInDays = 5))
		createTask(token, projectId, task("Mid-medium", "MEDIUM", dueInDays = 3))

		send(token, get("/api/projects/$projectId/tasks?sortBy=dueDate&direction=asc"))
			.andExpect(jsonPath("$.content[0].title").value("Soon-low"))
			.andExpect(jsonPath("$.content[2].title").value("Later-high"))

		send(token, get("/api/projects/$projectId/tasks?sortBy=dueDate&direction=desc"))
			.andExpect(jsonPath("$.content[0].title").value("Later-high"))

		send(token, get("/api/projects/$projectId/tasks?sortBy=priority&direction=desc"))
			.andExpect(jsonPath("$.content[0].title").value("Later-high"))
			.andExpect(jsonPath("$.content[2].title").value("Soon-low"))

		send(token, get("/api/projects/$projectId/tasks?sortBy=bogus"))
			.andExpect(status().isBadRequest)
	}

	@Test
	fun `tasks are paginated`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(token)
		repeat(3) { createTask(token, projectId, task("Task $it", dueInDays = it.toLong())) }

		send(token, get("/api/projects/$projectId/tasks?page=1&size=2"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.totalElements").value(3))
			.andExpect(jsonPath("$.totalPages").value(2))
			.andExpect(jsonPath("$.page").value(1))
	}

	@Test
	fun `a task can be updated even to a past due date, and its status changed and deleted`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(token)
		val id = createTaskId(token, projectId, task("Original", dueInDays = 2))

		send(
			token, put("/api/tasks/$id"),
			"""{"title":"Renamed","status":"TODO","priority":"LOW","dueDate":"${today.minusDays(1)}"}""",
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.title").value("Renamed"))
			.andExpect(jsonPath("$.priority").value("LOW"))
			.andExpect(jsonPath("$.overdue").value(true))

		send(token, patch("/api/tasks/$id/status"), """{"status":"DONE"}""")
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("DONE"))
			.andExpect(jsonPath("$.overdue").value(false))

		send(token, patch("/api/tasks/$id/status"), """{"status":"NOT_A_STATUS"}""")
			.andExpect(status().isBadRequest)

		send(token, delete("/api/tasks/$id")).andExpect(status().isNoContent)
		send(token, delete("/api/tasks/$id")).andExpect(status().isNotFound)
	}

	@Test
	fun `summary counts tasks per status and overdue tasks`() {
		val token = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(token)
		val overdue = createTaskId(token, projectId, task("Overdue", dueInDays = 1))
		val inProgress = createTaskId(token, projectId, task("Working"))
		val doneLate = createTaskId(token, projectId, task("Done late", dueInDays = 1))
		val yesterday = today.minusDays(1)

		send(token, put("/api/tasks/$overdue"), """{"title":"Overdue","status":"TODO","priority":"MEDIUM","dueDate":"$yesterday"}""")
		send(token, patch("/api/tasks/$inProgress/status"), """{"status":"IN_PROGRESS"}""")
		send(token, put("/api/tasks/$doneLate"), """{"title":"Done late","status":"DONE","priority":"MEDIUM","dueDate":"$yesterday"}""")

		send(token, get("/api/projects/$projectId/summary"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.total").value(3))
			.andExpect(jsonPath("$.byStatus.TODO").value(1))
			.andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(1))
			.andExpect(jsonPath("$.byStatus.DONE").value(1))
			.andExpect(jsonPath("$.overdue").value(1))
	}

	@Test
	fun `users cannot see or change tasks in projects they do not own`() {
		val alice = registerAndGetToken(mockMvc, objectMapper)
		val bob = registerAndGetToken(mockMvc, objectMapper)
		val projectId = createProjectId(alice)
		val taskId = createTaskId(alice, projectId, task("Private"))

		send(bob, get("/api/projects/$projectId/tasks")).andExpect(status().isNotFound)
		send(bob, get("/api/projects/$projectId/summary")).andExpect(status().isNotFound)
		createTask(bob, projectId, task("Intruder")).andExpect(status().isNotFound)
		send(bob, put("/api/tasks/$taskId"), """{"title":"Hacked","status":"DONE","priority":"LOW"}""")
			.andExpect(status().isNotFound)
		send(bob, patch("/api/tasks/$taskId/status"), """{"status":"DONE"}""").andExpect(status().isNotFound)
		send(bob, delete("/api/tasks/$taskId")).andExpect(status().isNotFound)

		send(alice, get("/api/projects/$projectId/tasks"))
			.andExpect(jsonPath("$.content[0].title").value("Private"))
	}

	@Test
	fun `users endpoint lists people who can be assigned`() {
		val user = registerUser(mockMvc, objectMapper, "Zed Assignee")

		send(user.token, get("/api/users"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[?(@.id == ${user.id})].name").value("Zed Assignee"))
			.andExpect(jsonPath("$[0].passwordHash").doesNotExist())
	}
}
