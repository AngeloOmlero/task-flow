package com.example.task_flow.controller

import com.example.task_flow.dto.AuthRequestDto
import com.example.task_flow.dto.AuthResponseDto
import com.example.task_flow.dto.CreateUserDto
import com.example.task_flow.dto.UserDto
import com.example.task_flow.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody createUserDto: CreateUserDto): UserDto {
        return authService.register(createUserDto)
    }

    @GetMapping("/users")
    fun getAllUsers(): List<UserDto> {
        return authService.getAllUsers()
    }

    @GetMapping("/users/search")
    fun searchUsers(@RequestParam query: String): List<UserDto> {
        return authService.searchUsers(query)
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody request : AuthRequestDto): AuthResponseDto{
        return authService.login(request)
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun getCurrentUser(@AuthenticationPrincipal userDetails: UserDetails): UserDto{
        return authService.getCurrentUser(userDetails.username)
    }
}