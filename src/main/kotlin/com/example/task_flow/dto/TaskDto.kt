package com.example.task_flow.dto


import com.example.task_flow.model.TaskPriority
import java.time.Instant
import java.time.LocalDate

data class TaskDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String, // Added status
    val priority: TaskPriority, // Added priority
    val dueDate: LocalDate?, // Added dueDate
    val assignees: Set<UserDto>, // Changed from single assignee to multiple assignees
    val boardId: Long, // Added boardId
    val createdAt: Instant, // Added createdAt
    val updatedAt: Instant // Added updatedAt
)
