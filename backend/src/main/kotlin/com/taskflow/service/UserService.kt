package com.taskflow.service

import com.taskflow.dto.UserResponse
import com.taskflow.dto.toResponse
import com.taskflow.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

	/** Everyone who can be chosen as a task assignee. */
	@Transactional(readOnly = true)
	fun listAll(): List<UserResponse> = userRepository.findAllByOrderByNameAsc().map { it.toResponse() }
}
