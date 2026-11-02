package com.taskflow.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Reads "Authorization: Bearer <token>" and, if the token is valid, marks the request
 * as authenticated. An invalid token simply leaves the request unauthenticated, and
 * Spring Security then answers 401 through the configured entry point.
 */
@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val header = request.getHeader(HttpHeaders.AUTHORIZATION)
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			jwtService.parse(header.removePrefix(BEARER_PREFIX).trim())?.let { user ->
				SecurityContextHolder.getContext().authentication =
					UsernamePasswordAuthenticationToken(user, null, emptyList())
			}
		}
		filterChain.doFilter(request, response)
	}

	private companion object {
		const val BEARER_PREFIX = "Bearer "
	}
}
