package com.example.task_flow.dto

import jakarta.validation.constraints.NotBlank

data class UpdateBoardDto(
    @field:NotBlank
    val title: String, // Changed from 'name' to 'title'
    val description: String? // Added description
)
