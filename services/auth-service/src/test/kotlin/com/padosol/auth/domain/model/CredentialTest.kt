package com.padosol.auth.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CredentialTest : StringSpec({

    "create() 는 ACTIVE 상태의 자격 증명을 생성하고 필드를 설정한다" {
        val credential = Credential.create("user@example.com", "hashed-pw")

        credential.email shouldBe "user@example.com"
        credential.passwordHash shouldBe "hashed-pw"
        credential.status shouldBe CredentialStatus.ACTIVE
    }

    "create() 는 호출마다 고유한 id 를 생성한다" {
        val a = Credential.create("a@example.com", "h")
        val b = Credential.create("b@example.com", "h")

        a.id shouldNotBe b.id
    }
})
