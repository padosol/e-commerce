package com.padosol.auth.adapter.`in`.web

import com.padosol.auth.application.exception.DuplicateEmailException
import com.padosol.auth.application.exception.InvalidCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("EMAIL_ALREADY_EXISTS", ex.message ?: "duplicate email"))

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("INVALID_CREDENTIALS", ex.message ?: "invalid credentials"))
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
