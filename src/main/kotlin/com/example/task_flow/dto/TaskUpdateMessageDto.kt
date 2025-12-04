package com.example.task_flow.dto


data class TaskUpdateMessageDto(
    val type: String, // TASK_CREATED, TASK_UPDATED, TASK_MOVED, TASK_DELETED, TASK_ASSIGNED
    val task: TaskDto
)
