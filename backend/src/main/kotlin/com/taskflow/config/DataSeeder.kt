package com.taskflow.config

import com.taskflow.domain.Project
import com.taskflow.domain.Task
import com.taskflow.domain.TaskPriority
import com.taskflow.domain.TaskStatus
import com.taskflow.domain.User
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import com.taskflow.service.ActivityService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/**
 * Creates a demo account with a sample project on first start, so reviewers can log in
 * and see a populated board straight away. It does nothing if the demo user already exists.
 * Disable with SEED_DEMO_DATA=false.
 */
@Component
@ConditionalOnProperty(name = ["app.seed.enabled"], havingValue = "true")
class DataSeeder(
	private val userRepository: UserRepository,
	private val projectRepository: ProjectRepository,
	private val taskRepository: TaskRepository,
	private val activityService: ActivityService,
	private val passwordEncoder: PasswordEncoder,
	private val clock: Clock,
) : ApplicationRunner {

	private val log = LoggerFactory.getLogger(javaClass)

	@Transactional
	override fun run(args: ApplicationArguments) {
		if (userRepository.existsByEmail(DEMO_EMAIL)) return

		val demo = createUser("Demo User", DEMO_EMAIL)
		val priya = createUser("Priya Sharma", "priya@taskflow.com")
		val today = LocalDate.now(clock)

		val website = projectRepository.save(
			Project("Website Redesign", "Refresh the marketing site before the Q1 launch.", demo),
		)
		// Dates are relative to today so the demo always shows upcoming, due-soon and overdue work.
		listOf(
			SeedTask("Design landing page", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, today.plusDays(3), demo),
			SeedTask("Set up CI pipeline", TaskPriority.MEDIUM, TaskStatus.TODO, today.plusDays(7), priya),
			SeedTask("Fix login redirect bug", TaskPriority.HIGH, TaskStatus.TODO, today.minusDays(2), demo),
			SeedTask("Write API documentation", TaskPriority.LOW, TaskStatus.TODO, null, null),
			SeedTask("Kick-off meeting notes", TaskPriority.LOW, TaskStatus.DONE, today.minusDays(5), priya),
			SeedTask("Review accessibility audit", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, today.plusDays(1), priya),
		).forEach { saveTask(website, it, demo) }

		val mobile = projectRepository.save(Project("Mobile App", "Companion app for iOS and Android.", demo))
		listOf(
			SeedTask("Choose a cross-platform framework", TaskPriority.HIGH, TaskStatus.DONE, today.minusDays(1), demo),
			SeedTask("Prototype the onboarding flow", TaskPriority.MEDIUM, TaskStatus.TODO, today.plusDays(10), priya),
		).forEach { saveTask(mobile, it, demo) }

		log.info("Seeded demo data. Log in with {} / {}", DEMO_EMAIL, DEMO_PASSWORD)
	}

	private data class SeedTask(
		val title: String,
		val priority: TaskPriority,
		val status: TaskStatus,
		val dueDate: LocalDate?,
		val assignee: User?,
	)

	private fun createUser(name: String, email: String): User =
		userRepository.save(User(name = name, email = email, passwordHash = passwordEncoder.encode(DEMO_PASSWORD)))

	private fun saveTask(project: Project, seed: SeedTask, actor: User) {
		val task = taskRepository.save(
			Task(
				title = seed.title,
				description = null,
				status = seed.status,
				priority = seed.priority,
				dueDate = seed.dueDate,
				assignee = seed.assignee,
				project = project,
			),
		)
		// Mirror what the API would have logged: creation, then the move to the current column.
		activityService.record(task, actor, from = null, to = TaskStatus.TODO)
		if (seed.status != TaskStatus.TODO) {
			activityService.record(task, actor, from = TaskStatus.TODO, to = seed.status)
		}
	}

	companion object {
		const val DEMO_EMAIL = "demo@taskflow.com"
		const val DEMO_PASSWORD = "Password123!"
	}
}
