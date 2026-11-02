package com.taskflow.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
	@Column(nullable = false)
	var name: String,

	@Column(nullable = false, unique = true)
	var email: String,

	@Column(name = "password_hash", nullable = false)
	var passwordHash: String,

	@Column(name = "created_at", nullable = false, updatable = false)
	val createdAt: Instant = Instant.now(),

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
