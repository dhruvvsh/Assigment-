package com.taskflow.exception

class NotFoundException(message: String) : RuntimeException(message)

/** A business-rule violation that is the caller's fault, optionally pointing at fields. */
class BadRequestException(
	message: String,
	val fieldErrors: List<FieldErrorDetail> = emptyList(),
) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)
