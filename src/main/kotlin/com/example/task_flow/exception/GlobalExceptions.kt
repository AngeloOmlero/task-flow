package com.example.task_flow.exception


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime


@ControllerAdvice
class GlobalExceptions {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<Any> {
        val body = mapOf("message" to ex.message)
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException, request: WebRequest): ResponseEntity<Any> {
        val body = mapOf("message" to ex.message)
        return ResponseEntity(body, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation Error", errors)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, Any>> =
        errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Unexpected error occurred")

    @ExceptionHandler(BadCredentialsException::class)
    fun handleCredentials(ex: BadCredentialsException) =
        errorResponse(HttpStatus.UNAUTHORIZED,ex.message?:"Invalid Credentials")

    @ExceptionHandler(UsernameNotFoundException::class)
    fun usernameNotFoundException(ex: UsernameNotFoundException) =
        errorResponse(HttpStatus.UNAUTHORIZED,ex.message?:"Invalid Username")

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        details:Any? = null
    ): ResponseEntity<Map<String, Any>>{
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now(),
            "error" to status.reasonPhrase,
            "status" to status.value(),
            "message" to message
        )
        details?.let { body["details"]= it }
        return ResponseEntity(body,status)
    }

}