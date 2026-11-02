package com.taskflow.security

import com.taskflow.domain.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

/** The identity extracted from a valid token; this is the security principal. */
data class AuthUser(val id: Long, val email: String)

@Service
class JwtService(private val properties: JwtProperties) {

	private val key = Keys.hmacShaKeyFor(properties.secret.toByteArray())

	fun generateToken(user: User): String {
		val now = Instant.now()
		return Jwts.builder()
			.subject(requireNotNull(user.id).toString())
			.claim("email", user.email)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusSeconds(properties.expirationMinutes * 60)))
			.signWith(key)
			.compact()
	}

	/** Returns the identity inside the token, or null if it is invalid or expired. */
	fun parse(token: String): AuthUser? = try {
		val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
		AuthUser(claims.subject.toLong(), claims.get("email", String::class.java))
	} catch (ex: JwtException) {
		null
	} catch (ex: IllegalArgumentException) {
		null
	}
}
