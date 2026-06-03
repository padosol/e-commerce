package com.padosol.auth.application.port.`in`

import java.util.UUID

/**
 * 회원가입 유스케이스. 신규 userId를 반환한다.
 */
interface SignUpUseCase {
    fun signUp(command: SignUpCommand): UUID
}

data class SignUpCommand(
    val email: String,
    val rawPassword: String,
)
