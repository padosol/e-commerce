package com.padosol.auth.application.service

import com.padosol.auth.application.exception.DuplicateEmailException
import com.padosol.auth.application.port.`in`.SignUpCommand
import com.padosol.auth.application.port.out.CredentialRepository
import com.padosol.auth.application.port.out.PasswordHasher
import com.padosol.auth.application.port.out.UserRegisteredEventPublisher
import com.padosol.auth.domain.model.Credential
import com.padosol.common.event.UserRegisteredEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class SignUpServiceTest : StringSpec({

    val repository = mockk<CredentialRepository>()
    val passwordHasher = mockk<PasswordHasher>()
    val eventPublisher = mockk<UserRegisteredEventPublisher>(relaxed = true)
    val service = SignUpService(repository, passwordHasher, eventPublisher)

    "이미 가입된 이메일이면 저장·발행 없이 예외를 던진다" {
        every { repository.existsByEmail("dup@example.com") } returns true

        shouldThrow<DuplicateEmailException> {
            service.signUp(SignUpCommand("dup@example.com", "pw12345!"))
        }
        verify(exactly = 0) { repository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    "신규 가입 시 비밀번호 해시·저장 후 UserRegistered 를 발행하고 userId 를 반환한다" {
        every { repository.existsByEmail("new@example.com") } returns false
        every { passwordHasher.hash("pw12345!") } returns "hashed"
        val saved = Credential.create("new@example.com", "hashed")
        every { repository.save(any()) } returns saved
        val eventSlot = slot<UserRegisteredEvent>()
        every { eventPublisher.publish(capture(eventSlot)) } returns Unit

        val userId = service.signUp(SignUpCommand("new@example.com", "pw12345!"))

        userId shouldBe saved.id
        verify { passwordHasher.hash("pw12345!") }
        eventSlot.captured.userId shouldBe saved.id.toString()
        eventSlot.captured.email shouldBe "new@example.com"
    }
})
