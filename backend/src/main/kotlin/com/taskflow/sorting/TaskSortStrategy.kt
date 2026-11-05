package com.taskflow.sorting

import com.taskflow.exception.BadRequestException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

/**
 * Strategy pattern: each way of ordering tasks is its own class behind one interface.
 * Adding a new sort option means adding one more strategy bean; the service and
 * controller do not change.
 */
interface TaskSortStrategy {
	/** The value of the `sortBy` query parameter that selects this strategy. */
	val key: String

	fun toSort(direction: Sort.Direction): Sort
}

@Component
class DueDateSortStrategy : TaskSortStrategy {
	override val key = "dueDate"

	// "id" as a tie-breaker keeps page boundaries stable when due dates are equal.
	// PostgreSQL places tasks without a due date last when ascending.
	override fun toSort(direction: Sort.Direction): Sort =
		Sort.by(Sort.Order(direction, "dueDate"), Sort.Order.asc("id"))
}

@Component
class PrioritySortStrategy : TaskSortStrategy {
	override val key = "priority"

	// Priority is stored by ordinal (LOW < MEDIUM < HIGH), so DESC puts HIGH first.
	// Within the same priority, the task due soonest comes first.
	override fun toSort(direction: Sort.Direction): Sort =
		Sort.by(Sort.Order(direction, "priority"), Sort.Order.asc("dueDate"), Sort.Order.asc("id"))
}

/** Picks the strategy that matches the requested `sortBy` value. */
@Component
class TaskSortResolver(strategies: List<TaskSortStrategy>) {

	private val byKey = strategies.associateBy { it.key }

	fun resolve(sortBy: String, direction: Sort.Direction): Sort {
		val strategy = byKey[sortBy] ?: throw BadRequestException(
			"Unsupported sortBy '$sortBy'. Allowed values: ${byKey.keys.sorted().joinToString()}",
		)
		return strategy.toSort(direction)
	}
}
