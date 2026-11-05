package com.taskflow.dto

import com.taskflow.domain.TaskActivity
import com.taskflow.domain.TaskStatus
import java.time.Instant

data class ActivityResponse(
	val id: Long,
	val fromStatus: TaskStatus?,
	val toStatus: TaskStatus,
	val changedBy: UserResponse,
	val changedAt: Instant,
)

fun TaskActivity.toResponse() = ActivityResponse(
	id = requireNotNull(id) { "Activity must be persisted before mapping" },
	fromStatus = fromStatus,
	toStatus = toStatus,
	changedBy = changedBy.toResponse(),
	changedAt = changedAt,
)
