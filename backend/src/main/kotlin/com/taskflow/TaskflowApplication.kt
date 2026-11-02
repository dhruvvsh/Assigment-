package com.taskflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskflowApplication

fun main(args: Array<String>) {
	runApplication<TaskflowApplication>(*args)
}
