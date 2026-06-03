package com.padosol.auth.application.port.`in`

/**
 * 로그인 유스케이스. 인증 성공 시 토큰 쌍을 발급한다.
 */
interface LoginUseCase {
    fun login(command: LoginCommand): Tokens
}

data class LoginCommand(
    val email: String,
    val rawPassword: String,
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
)
