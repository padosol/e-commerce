package com.padosol.auth.application.service

import com.padosol.auth.application.exception.InvalidCredentialsException
import com.padosol.auth.application.port.`in`.LoginCommand
import com.padosol.auth.application.port.`in`.Tokens
import com.padosol.auth.application.port.out.CredentialRepository
import com.padosol.auth.application.port.out.PasswordHasher
import com.padosol.auth.application.port.out.TokenIssuer
import com.padosol.auth.domain.model.Credential
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class LoginServiceTest : StringSpec({

    val repository = mockk<CredentialRepository>()
    val passwordHasher = mockk<PasswordHasher>()
    val tokenIssuer = mockk<TokenIssuer>()
    val service = LoginService(repository, passwordHasher, tokenIssuer)

    "이메일이 없으면 InvalidCredentialsException 을 던진다" {
        every { repository.findByEmail("none@example.com") } returns null

        shouldThrow<InvalidCredentialsException> {
            service.login(LoginCommand("none@example.com", "pw12345!"))
        }
    }

    "비밀번호가 일치하지 않으면 InvalidCredentialsException 을 던진다" {
        val credential = Credential.create("u@example.com", "hashed")
        every { repository.findByEmail("u@example.com") } returns credential
        every { passwordHasher.matches("wrong", "hashed") } returns false

        shouldThrow<InvalidCredentialsException> {
            service.login(LoginCommand("u@example.com", "wrong"))
        }
    }

    "인증에 성공하면 토큰을 발급한다" {
        val credential = Credential.create("u@example.com", "hashed")
        every { repository.findByEmail("u@example.com") } returns credential
        every { passwordHasher.matches("pw12345!", "hashed") } returns true
        val tokens = Tokens("access", "refresh", 900)
        every { tokenIssuer.issueTokens(credential.id, "u@example.com") } returns tokens

        service.login(LoginCommand("u@example.com", "pw12345!")) shouldBe tokens
    }
})
