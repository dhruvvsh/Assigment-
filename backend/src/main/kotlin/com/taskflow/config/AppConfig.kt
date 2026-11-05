package com.taskflow.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class AppConfig {

	/** Injected wherever "today" matters (overdue checks) so tests can fix the date. */
	@Bean
	fun clock(): Clock = Clock.systemDefaultZone()
}
