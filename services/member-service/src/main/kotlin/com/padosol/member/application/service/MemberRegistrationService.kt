package com.padosol.member.application.service

import com.padosol.member.application.port.`in`.RegisterMemberOnUserRegistered
import com.padosol.member.application.port.out.MemberRepository
import com.padosol.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class MemberRegistrationService(
    private val memberRepository: MemberRepository,
) : RegisterMemberOnUserRegistered {

    override fun handle(userId: UUID, email: String) {
        if (memberRepository.existsByUserId(userId)) {
            return
        }
        memberRepository.save(Member.register(userId, email))
    }
}
