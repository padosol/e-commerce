package com.padosol.member.application.service

import com.padosol.member.application.exception.MemberNotFoundException
import com.padosol.member.application.port.`in`.GetMyProfile
import com.padosol.member.application.port.`in`.MemberProfile
import com.padosol.member.application.port.out.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository: MemberRepository,
) : GetMyProfile {

    override fun byUserId(userId: UUID): MemberProfile {
        val member = memberRepository.findByUserId(userId)
            ?: throw MemberNotFoundException(userId)
        return MemberProfile(
            userId = member.userId,
            email = member.email,
            nickname = member.nickname,
            grade = member.grade,
            addresses = member.addresses.toList(),
        )
    }
}
