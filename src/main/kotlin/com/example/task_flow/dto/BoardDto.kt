package com.example.task_flow.dto

import java.time.Instant

data class BoardDto(
    val id: Long,
    val title: String, // Changed from 'name' to 'title'
    val description: String?, // Added description
    val owner: UserDto,
    val members: Set<UserDto>,
    val createdAt: Instant, // Added createdAt
    val updatedAt: Instant // Added updatedAt
)
