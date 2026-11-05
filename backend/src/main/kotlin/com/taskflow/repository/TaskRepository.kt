package com.taskflow.repository

import com.taskflow.domain.Task
import com.taskflow.domain.TaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface StatusCount {
	val status: TaskStatus
	val total: Long
}

interface TaskRepository : JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	/** Fetch assignees together with the page of tasks to avoid one extra query per row. */
	@EntityGraph(attributePaths = ["assignee"])
	override fun findAll(spec: Specification<Task>?, pageable: Pageable): Page<Task>

	/** Ownership is enforced in the query: the task's project must belong to [ownerId]. */
	fun findByIdAndProjectOwnerId(id: Long, ownerId: Long): Task?

	@Query("select t.status as status, count(t) as total from Task t where t.project.id = :projectId group by t.status")
	fun countByStatus(projectId: Long): List<StatusCount>

	fun countByProjectIdAndStatusNotAndDueDateBefore(projectId: Long, status: TaskStatus, date: LocalDate): Long
}
