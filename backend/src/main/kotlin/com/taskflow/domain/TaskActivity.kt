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
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

/** One row per status change of a task: who moved it, from where to where, and when. */
@Entity
@Table(name = "task_activities")
class TaskActivity(
	// Deleting a task also deletes its history, enforced by the database foreign key.
	@OnDelete(action = OnDeleteAction.CASCADE)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	val task: Task,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "changed_by_id", nullable = false)
	val changedBy: User,

	/** Null when the task was just created. */
	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 20)
	val fromStatus: TaskStatus?,

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 20)
	val toStatus: TaskStatus,

	@Column(name = "changed_at", nullable = false, updatable = false)
	val changedAt: Instant = Instant.now(),

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
