package com.padosol.auth.application.service

import com.padosol.auth.application.exception.DuplicateEmailException
import com.padosol.auth.application.port.`in`.SignUpCommand
import com.padosol.auth.application.port.`in`.SignUpUseCase
import com.padosol.auth.application.port.out.CredentialRepository
import com.padosol.auth.application.port.out.PasswordHasher
import com.padosol.auth.application.port.out.UserRegisteredEventPublisher
import com.padosol.auth.domain.model.Credential
import com.padosol.common.event.UserRegisteredEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SignUpService(
    private val credentialRepository: CredentialRepository,
    private val passwordHasher: PasswordHasher,
    private val eventPublisher: UserRegisteredEventPublisher,
) : SignUpUseCase {

    @Transactional
    override fun signUp(command: SignUpCommand): UUID {
        if (credentialRepository.existsByEmail(command.email)) {
            throw DuplicateEmailException(command.email)
        }

        val passwordHash = passwordHasher.hash(command.rawPassword)
        val credential = credentialRepository.save(
            Credential.create(email = command.email, passwordHash = passwordHash),
        )

        eventPublisher.publish(
            UserRegisteredEvent(credential.id.toString(), credential.email, Instant.now()),
        )

        return credential.id
    }
}
