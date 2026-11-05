package com.taskflow.repository

import com.taskflow.domain.Task
import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import org.springframework.data.jpa.domain.Specification

/**
 * Small composable query filters. Each optional filter is only added when a value is given,
 * so the repository query is built from exactly the criteria the caller asked for.
 */
object TaskSpecifications {

	fun forProject(projectId: Long): Specification<Task> =
		Specification { root, _, cb -> cb.equal(root.get<Any>("project").get<Long>("id"), projectId) }

	fun hasStatus(status: TaskStatus?): Specification<Task>? = status?.let {
		Specification { root, _, cb -> cb.equal(root.get<TaskStatus>("status"), it) }
	}

	fun hasPriority(priority: TaskPriority?): Specification<Task>? = priority?.let {
		Specification { root, _, cb -> cb.equal(root.get<TaskPriority>("priority"), it) }
	}

	fun hasAssignee(assigneeId: Long?): Specification<Task>? = assigneeId?.let {
		Specification { root, _, cb -> cb.equal(root.get<Any>("assignee").get<Long>("id"), it) }
	}
}
