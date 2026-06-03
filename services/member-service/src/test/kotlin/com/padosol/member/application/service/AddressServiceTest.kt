package com.padosol.member.application.service

import com.padosol.member.application.exception.MemberNotFoundException
import com.padosol.member.application.port.out.MemberRepository
import com.padosol.member.domain.model.Address
import com.padosol.member.domain.model.Member
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID

class AddressServiceTest : StringSpec({

    val repository = mockk<MemberRepository>()
    val service = AddressService(repository)
    val userId = UUID.randomUUID()

    "회원이 없으면 MemberNotFoundException 을 던진다" {
        every { repository.findByUserId(userId) } returns null

        shouldThrow<MemberNotFoundException> {
            service.addAddress(userId, Address("12345", "도로명 1", null))
        }
    }

    "회원에 배송지를 추가하고 저장한다" {
        val member = Member.register(userId, "user@example.com")
        every { repository.findByUserId(userId) } returns member
        every { repository.save(member) } returns member

        service.addAddress(userId, Address("12345", "서울특별시 1로", "201호"))

        member.addresses.size shouldBe 1
        verify { repository.save(member) }
    }
})
