package com.padosol.auth.adapter.`in`.web

import com.padosol.auth.application.port.`in`.LoginCommand
import com.padosol.auth.application.port.`in`.LoginUseCase
import com.padosol.auth.application.port.`in`.SignUpCommand
import com.padosol.auth.application.port.`in`.SignUpUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
) {

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<SignUpResponse> {
        val userId = signUpUseCase.signUp(SignUpCommand(request.email, request.password))
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponse(userId))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val tokens = loginUseCase.login(LoginCommand(request.email, request.password))
        return LoginResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            expiresInSeconds = tokens.expiresInSeconds,
        )
    }
}

data class SignUpRequest(
    @field:Email
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
)

data class SignUpResponse(
    val userId: UUID,
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
)
