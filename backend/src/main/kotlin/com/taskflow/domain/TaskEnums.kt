package com.taskflow.domain

enum class TaskStatus { TODO, IN_PROGRESS, DONE }

/**
 * Declaration order is meaningful: Task stores priority by ordinal, so sorting by the
 * column gives LOW < MEDIUM < HIGH. Do not reorder these values.
 */
enum class TaskPriority { LOW, MEDIUM, HIGH }
