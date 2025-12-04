package com.example.task_flow.dto

import java.time.Instant

data class CommentDto(
    val id: Long,
    val content: String,
    val author: UserDto,
    val taskId: Long, // Added taskId
    val createdAt: Instant, // Added createdAt
    val updatedAt: Instant // Added updatedAt
)
