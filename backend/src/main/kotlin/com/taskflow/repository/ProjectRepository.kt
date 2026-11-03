package com.taskflow.repository

import com.taskflow.domain.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long> {
	fun findAllByOwnerIdOrderByCreatedAtDesc(ownerId: Long): List<Project>

	/** Scoping the lookup by owner is what enforces "users only see their own projects". */
	fun findByIdAndOwnerId(id: Long, ownerId: Long): Project?
}
