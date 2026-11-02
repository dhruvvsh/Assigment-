package com.taskflow.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.taskflow.exception.ApiError
import com.taskflow.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
	private val jwtAuthFilter: JwtAuthFilter,
	private val objectMapper: ObjectMapper,
) {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			// Stateless token API: no session cookies, so CSRF protection does not apply.
			.csrf { it.disable() }
			.cors(Customizer.withDefaults())
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests {
				it.requestMatchers("/api/auth/**").permitAll()
				it.anyRequest().authenticated()
			}
			.exceptionHandling {
				it.authenticationEntryPoint { request, response, _ ->
					val body = ApiError(
						status = HttpStatus.UNAUTHORIZED.value(),
						error = HttpStatus.UNAUTHORIZED.reasonPhrase,
						message = "Authentication is required or the token is invalid or expired",
						path = request.requestURI,
					)
					response.status = HttpStatus.UNAUTHORIZED.value()
					response.contentType = MediaType.APPLICATION_JSON_VALUE
					objectMapper.writeValue(response.outputStream, body)
				}
			}
			.httpBasic(AbstractHttpConfigurer<*, *>::disable)
			.formLogin(AbstractHttpConfigurer<*, *>::disable)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
		return http.build()
	}
}
