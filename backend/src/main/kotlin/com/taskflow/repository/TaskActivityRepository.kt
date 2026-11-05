package com.taskflow.repository

import com.taskflow.domain.TaskActivity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface TaskActivityRepository : JpaRepository<TaskActivity, Long> {

	@EntityGraph(attributePaths = ["changedBy"])
	fun findAllByTaskIdOrderByChangedAtDescIdDesc(taskId: Long): List<TaskActivity>
}
