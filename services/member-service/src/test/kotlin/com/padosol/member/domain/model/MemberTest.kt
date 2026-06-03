package com.padosol.member.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class MemberTest : StringSpec({

    "register 는 이메일 local-part 로 닉네임을 만들고 BRONZE 로 시작한다" {
        val userId = UUID.randomUUID()

        val member = Member.register(userId, "hong.gildong@example.com")

        member.userId shouldBe userId
        member.email shouldBe "hong.gildong@example.com"
        member.nickname shouldBe "hong.gildong"
        member.grade shouldBe MemberGrade.BRONZE
        member.addresses.size shouldBe 0
    }

    "addAddress 는 배송지를 추가한다" {
        val member = Member.register(UUID.randomUUID(), "u@example.com")

        member.addAddress(Address("12345", "도로명 1", null))

        member.addresses.size shouldBe 1
    }
})
