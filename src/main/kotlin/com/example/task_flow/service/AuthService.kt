package com.example.task_flow.service




import com.example.task_flow.dto.AuthRequestDto
import com.example.task_flow.dto.AuthResponseDto
import com.example.task_flow.dto.CreateUserDto
import com.example.task_flow.dto.UserDto
import com.example.task_flow.model.User
import com.example.task_flow.repository.UserRepository
import com.example.task_flow.security.JWTUtil
import com.example.task_flow.security.UserDetailsServiceImpl
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.collections.distinctBy
import kotlin.collections.map
import kotlin.let
import kotlin.text.toLong

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsServiceImpl,
) {

    fun register(createUserDto: CreateUserDto): UserDto {
        userRepository.findByUsername(createUserDto.username)
            ?.let { throw kotlin.IllegalArgumentException("Username ${createUserDto.username} is already in use") }

        val createUser = User(
            username = createUserDto.username,
            password = passwordEncoder.encode(createUserDto.password)
        )

        val savedUser = userRepository.save(createUser)

        return UserDto(savedUser.id, savedUser.username)

    }


    fun getCurrentUser(username: String): UserDto {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return UserDto(user.id, user.username)
    }


    fun login(request: AuthRequestDto): AuthResponseDto {
        return try {
            userDetailsService.loadUserByUsername(request.username)

            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val userDetails = authentication.principal as UserDetails
            val token = jwtUtil.generateToken(userDetails)

            AuthResponseDto(token)

        }catch (ex: Exception) {
            when (ex) {
                is BadCredentialsException -> throw BadCredentialsException("Invalid username or password")
                is UsernameNotFoundException -> throw UsernameNotFoundException("Invalid username: ${request.username}")
                else -> throw kotlin.RuntimeException("Authentication failed: ${ex.message}")
            }

        }

    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { UserDto(it.id, it.username) }
    }



    fun searchUsers(query: String): List<UserDto> {
        val results = mutableListOf<User>()


        try {
            val id = query.toLong()
            userRepository.findById(id).ifPresent { results.add(it) }
        } catch (e: NumberFormatException) {
        }


        val stringResults = userRepository.searchUsers(query)
        results.addAll(stringResults)


        return results.distinctBy { it.id }.map { UserDto(it.id, it.username) }
    }
}