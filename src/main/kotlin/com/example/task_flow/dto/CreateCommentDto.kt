package com.example.task_flow.dto

import jakarta.validation.constraints.NotBlank

data class CreateCommentDto(
    @field:NotBlank
    val content: String
)