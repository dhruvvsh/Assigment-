package com.taskflow.service

import com.taskflow.domain.Project
import com.taskflow.domain.User
import com.taskflow.dto.CreateProjectRequest
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ProjectServiceTest {

	private val projectRepository = mockk<ProjectRepository>()
	private val userRepository = mockk<UserRepository>()
	private val service = ProjectService(projectRepository, userRepository)

	private val owner = User(name = "Ann", email = "ann@test.com", passwordHash = "hashed", id = 1L)

	@Test
	fun `create trims the input, clears a blank description and assigns the caller as owner`() {
		val saved = slot<Project>()
		every { userRepository.getReferenceById(1L) } returns owner
		every { projectRepository.save(capture(saved)) } answers {
			val p = saved.captured
			Project(p.name, p.description, p.owner, p.createdAt, id = 7L)
		}

		val response = service.create(1L, CreateProjectRequest("  Website  ", "   "))

		assertEquals("Website", saved.captured.name)
		assertNull(saved.captured.description)
		assertSame(owner, saved.captured.owner)
		assertEquals(7L, response.id)
		assertEquals(1L, response.owner.id)
	}

	@Test
	fun `listForUser returns only the projects the repository finds for that owner`() {
		every { projectRepository.findAllByOwnerIdOrderByCreatedAtDesc(1L) } returns
			listOf(Project("A", null, owner, id = 1L), Project("B", "desc", owner, id = 2L))

		val result = service.listForUser(1L)

		assertEquals(listOf("A", "B"), result.map { it.name })
	}

	@Test
	fun `getOwnedProject reports someone else's project as not found`() {
		every { projectRepository.findByIdAndOwnerId(5L, 2L) } returns null

		assertFailsWith<NotFoundException> { service.getOwnedProject(5L, 2L) }
	}

	@Test
	fun `getOwnedProject returns the project for its owner`() {
		val project = Project("A", null, owner, id = 5L)
		every { projectRepository.findByIdAndOwnerId(5L, 1L) } returns project

		assertSame(project, service.getOwnedProject(5L, 1L))
	}
}
