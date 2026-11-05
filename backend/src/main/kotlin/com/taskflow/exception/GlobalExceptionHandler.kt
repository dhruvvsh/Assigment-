package com.taskflow.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Translates every exception into the [ApiError] shape.
 *
 * Extending [ResponseEntityExceptionHandler] gives us Spring's built-in handling of
 * framework errors (malformed JSON, wrong method, missing parameter...); we only
 * override how those are rendered so the response shape stays consistent.
 */
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun handleMethodArgumentNotValid(
		ex: MethodArgumentNotValidException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any> {
		val fieldErrors = ex.bindingResult.fieldErrors.map {
			FieldErrorDetail(it.field, it.defaultMessage ?: "Invalid value")
		}
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request.path(), fieldErrors)
	}

	override fun handleExceptionInternal(
		ex: Exception,
		body: Any?,
		headers: HttpHeaders,
		statusCode: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val message = if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
			"Malformed or invalid request"
		} else {
			HttpStatus.resolve(statusCode.value())?.reasonPhrase ?: "Request failed"
		}
		return build(statusCode, message, request.path())
	}

	@ExceptionHandler(BadRequestException::class)
	fun handleBadRequest(ex: BadRequestException, request: HttpServletRequest) =
		build(HttpStatus.BAD_REQUEST, ex.message, request.requestURI, ex.fieldErrors)

	@ExceptionHandler(NotFoundException::class)
	fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) =
		build(HttpStatus.NOT_FOUND, ex.message, request.requestURI)

	@ExceptionHandler(ConflictException::class)
	fun handleConflict(ex: ConflictException, request: HttpServletRequest) =
		build(HttpStatus.CONFLICT, ex.message, request.requestURI)

	@ExceptionHandler(UnauthorizedException::class)
	fun handleUnauthorized(ex: UnauthorizedException, request: HttpServletRequest) =
		build(HttpStatus.UNAUTHORIZED, ex.message, request.requestURI)

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(ex: Exception, request: HttpServletRequest): ResponseEntity<Any> {
		log.error("Unhandled exception on {}", request.requestURI, ex)
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.requestURI)
	}

	private fun build(
		status: HttpStatusCode,
		message: String?,
		path: String,
		fieldErrors: List<FieldErrorDetail> = emptyList(),
	): ResponseEntity<Any> {
		val reason = HttpStatus.resolve(status.value())?.reasonPhrase ?: "Error"
		val body = ApiError(
			status = status.value(),
			error = reason,
			message = message ?: reason,
			path = path,
			fieldErrors = fieldErrors,
		)
		return ResponseEntity.status(status).body(body)
	}

	private fun WebRequest.path(): String = (this as ServletWebRequest).request.requestURI
}
