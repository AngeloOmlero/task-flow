package com.example.task_flow.dto

import com.example.task_flow.model.TaskPriority
import java.time.LocalDate

data class UpdateTaskDto(
    val title: String?, // Made nullable
    val description: String?,
    val status: String?, // Added status
    val priority: TaskPriority?, // Added priority
    val dueDate: LocalDate?, // Added dueDate
    val assigneeIds: Set<Long>? // Changed from single assigneeId to multiple assigneeIds
)
