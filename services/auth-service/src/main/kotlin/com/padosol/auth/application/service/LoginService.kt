package com.padosol.auth.application.service

import com.padosol.auth.application.exception.InvalidCredentialsException
import com.padosol.auth.application.port.`in`.LoginCommand
import com.padosol.auth.application.port.`in`.LoginUseCase
import com.padosol.auth.application.port.`in`.Tokens
import com.padosol.auth.application.port.out.CredentialRepository
import com.padosol.auth.application.port.out.PasswordHasher
import com.padosol.auth.application.port.out.TokenIssuer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val credentialRepository: CredentialRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
) : LoginUseCase {

    @Transactional(readOnly = true)
    override fun login(command: LoginCommand): Tokens {
        val credential = credentialRepository.findByEmail(command.email)
            ?: throw InvalidCredentialsException()

        if (!passwordHasher.matches(command.rawPassword, credential.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return tokenIssuer.issueTokens(credential.id, credential.email)
    }
}
