package com.taskflow.service

import com.taskflow.domain.Task
import com.taskflow.domain.TaskActivity
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import com.taskflow.dto.ActivityResponse
import com.taskflow.dto.toResponse
import com.taskflow.exception.NotFoundException
import com.taskflow.repository.TaskActivityRepository
import com.taskflow.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityService(
	private val activityRepository: TaskActivityRepository,
	private val taskRepository: TaskRepository,
) {

	/** Joins the caller's transaction, so the log entry is saved together with the change itself. */
	@Transactional
	fun record(task: Task, actor: User, from: TaskStatus?, to: TaskStatus) {
		activityRepository.save(TaskActivity(task = task, changedBy = actor, fromStatus = from, toStatus = to))
	}

	/** Newest first. Only the owner of the task's project can read its history. */
	@Transactional(readOnly = true)
	fun listForTask(taskId: Long, userId: Long): List<ActivityResponse> {
		taskRepository.findByIdAndProjectOwnerId(taskId, userId)
			?: throw NotFoundException("Task $taskId not found")
		return activityRepository.findAllByTaskIdOrderByChangedAtDescIdDesc(taskId).map { it.toResponse() }
	}
}
