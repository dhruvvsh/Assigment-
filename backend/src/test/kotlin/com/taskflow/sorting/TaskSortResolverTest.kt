package com.taskflow.sorting

import com.taskflow.exception.BadRequestException
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TaskSortResolverTest {

	private val resolver = TaskSortResolver(listOf(DueDateSortStrategy(), PrioritySortStrategy()))

	@Test
	fun `dueDate sorts by due date in the requested direction with an id tie-breaker`() {
		val orders = resolver.resolve("dueDate", Sort.Direction.DESC).toList()

		assertEquals(listOf("dueDate", "id"), orders.map { it.property })
		assertEquals(Sort.Direction.DESC, orders[0].direction)
		assertEquals(Sort.Direction.ASC, orders[1].direction)
	}

	@Test
	fun `priority sorts by priority first, then soonest due date`() {
		val orders = resolver.resolve("priority", Sort.Direction.DESC).toList()

		assertEquals(listOf("priority", "dueDate", "id"), orders.map { it.property })
		assertEquals(Sort.Direction.DESC, orders[0].direction)
		assertEquals(Sort.Direction.ASC, orders[1].direction)
	}

	@Test
	fun `an unknown sort key is rejected and the allowed keys are listed`() {
		val error = assertFailsWith<BadRequestException> { resolver.resolve("title", Sort.Direction.ASC) }

		assertTrue(error.message!!.contains("dueDate, priority"))
	}
}
