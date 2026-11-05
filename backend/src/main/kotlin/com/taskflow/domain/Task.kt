package com.taskflow.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "tasks")
class Task(
	@Column(nullable = false, length = 120)
	var title: String,

	@Column(length = 2000)
	var description: String?,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	var status: TaskStatus = TaskStatus.TODO,

	// ORDINAL on purpose, so ORDER BY priority sorts LOW < MEDIUM < HIGH (see TaskPriority).
	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	var priority: TaskPriority = TaskPriority.MEDIUM,

	@Column(name = "due_date")
	var dueDate: LocalDate? = null,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	var assignee: User? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	val project: Project,

	@Column(name = "created_at", nullable = false, updatable = false)
	val createdAt: Instant = Instant.now(),

	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant = Instant.now(),

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
) {
	@PreUpdate
	fun touch() {
		updatedAt = Instant.now()
	}
}
