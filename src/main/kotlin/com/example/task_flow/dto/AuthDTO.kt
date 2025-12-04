package com.example.task_flow.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserDto(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 30, message = "Username must be 3–30 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    val password: String
)

data class AuthRequestDto(
    val username: String,
    val password: String
)

data class AuthResponseDto(
    val token: String
)

