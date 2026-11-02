package com.taskflow.exception

import java.time.Instant

/** The single error shape returned by every failing endpoint. */
data class ApiError(
	val timestamp: Instant = Instant.now(),
	val status: Int,
	val error: String,
	val message: String,
	val path: String,
	val fieldErrors: List<FieldErrorDetail> = emptyList(),
)

data class FieldErrorDetail(
	val field: String,
	val message: String,
)
