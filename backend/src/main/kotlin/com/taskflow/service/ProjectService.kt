package com.taskflow.service

import com.taskflow.domain.Project
import com.taskflow.dto.CreateProjectRequest
import com.taskflow.dto.ProjectResponse
import com.taskflow.dto.toResponse
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
	private val projectRepository: ProjectRepository,
	private val userRepository: UserRepository,
) {

	@Transactional(readOnly = true)
	fun listForUser(userId: Long): List<ProjectResponse> =
		projectRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

	@Transactional
	fun create(userId: Long, request: CreateProjectRequest): ProjectResponse {
		val owner = userRepository.getReferenceById(userId)
		val project = projectRepository.save(
			Project(
				name = request.name.trim(),
				description = request.description?.trim()?.ifBlank { null },
				owner = owner,
			),
		)
		return project.toResponse()
	}

	/**
	 * Loads a project only if [userId] owns it. Someone else's project is reported as
	 * "not found" (404, not 403) so its existence is not revealed. Task operations reuse this.
	 */
	@Transactional(readOnly = true)
	fun getOwnedProject(projectId: Long, userId: Long): Project =
		projectRepository.findByIdAndOwnerId(projectId, userId)
			?: throw NotFoundException("Project $projectId not found")
}
