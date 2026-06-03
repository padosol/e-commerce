package com.padosol.member.application.service

import com.padosol.member.application.port.out.MemberRepository
import com.padosol.member.domain.model.Member
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID

class MemberRegistrationServiceTest : StringSpec({

    val repository = mockk<MemberRepository>()
    val service = MemberRegistrationService(repository)
    val userId = UUID.randomUUID()

    "이미 존재하는 회원이면 저장하지 않는다(멱등)" {
        every { repository.existsByUserId(userId) } returns true

        service.handle(userId, "u@example.com")

        verify(exactly = 0) { repository.save(any()) }
    }

    "신규면 register 한 회원을 저장한다" {
        every { repository.existsByUserId(userId) } returns false
        val saved = slot<Member>()
        every { repository.save(capture(saved)) } answers { saved.captured }

        service.handle(userId, "newbie@example.com")

        verify(exactly = 1) { repository.save(any()) }
        saved.captured.userId shouldBe userId
        saved.captured.email shouldBe "newbie@example.com"
        saved.captured.nickname shouldBe "newbie"
    }
})
