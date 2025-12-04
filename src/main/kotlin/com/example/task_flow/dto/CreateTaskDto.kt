package com.example.task_flow.dto


import com.example.task_flow.model.TaskPriority
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class CreateTaskDto(
    @field:NotBlank
    val title: String,
    val description: String?,
    val status: String = "TODO", // Added status with default
    val priority: TaskPriority = TaskPriority.LOW, // Added priority with default
    val dueDate: LocalDate? = null, // Added dueDate
    val assigneeIds: Set<Long>? = null // Added assigneeIds
)
