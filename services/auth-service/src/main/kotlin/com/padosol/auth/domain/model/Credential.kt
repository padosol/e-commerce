package com.padosol.auth.domain.model

import java.util.UUID

/**
 * 인증 자격 증명 애그리거트.
 * id(UUID)가 곧 userId이며, 발급되는 토큰의 sub 클레임으로 사용된다.
 */
class Credential(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val status: CredentialStatus,
) {
    companion object {
        /**
         * 새 ACTIVE 자격 증명을 생성한다. 비밀번호는 이미 해시된 값을 받는다.
         */
        fun create(email: String, passwordHash: String): Credential =
            Credential(
                id = UUID.randomUUID(),
                email = email,
                passwordHash = passwordHash,
                status = CredentialStatus.ACTIVE,
            )
    }
}

enum class CredentialStatus {
    ACTIVE,
}
