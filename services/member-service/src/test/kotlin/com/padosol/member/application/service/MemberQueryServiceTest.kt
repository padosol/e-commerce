package com.padosol.member.application.service

import com.padosol.member.application.exception.MemberNotFoundException
import com.padosol.member.application.port.out.MemberRepository
import com.padosol.member.domain.model.Member
import com.padosol.member.domain.model.MemberGrade
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class MemberQueryServiceTest : StringSpec({

    val repository = mockk<MemberRepository>()
    val service = MemberQueryService(repository)
    val userId = UUID.randomUUID()

    "회원이 없으면 MemberNotFoundException 을 던진다" {
        every { repository.findByUserId(userId) } returns null

        shouldThrow<MemberNotFoundException> {
            service.byUserId(userId)
        }
    }

    "회원이 있으면 프로필 뷰를 반환한다" {
        every { repository.findByUserId(userId) } returns Member.register(userId, "user@example.com")

        val profile = service.byUserId(userId)

        profile.userId shouldBe userId
        profile.email shouldBe "user@example.com"
        profile.nickname shouldBe "user"
        profile.grade shouldBe MemberGrade.BRONZE
    }
})
