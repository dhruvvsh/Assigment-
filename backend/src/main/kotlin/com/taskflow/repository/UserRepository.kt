package com.taskflow.repository

import com.taskflow.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
	fun findByEmail(email: String): User?
	fun existsByEmail(email: String): Boolean
	fun findAllByOrderByNameAsc(): List<User>
}
