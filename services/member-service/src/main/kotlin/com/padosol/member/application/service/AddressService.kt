package com.padosol.member.application.service

import com.padosol.member.application.exception.MemberNotFoundException
import com.padosol.member.application.port.`in`.AddAddressUseCase
import com.padosol.member.application.port.out.MemberRepository
import com.padosol.member.domain.model.Address
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AddressService(
    private val memberRepository: MemberRepository,
) : AddAddressUseCase {

    override fun addAddress(userId: UUID, address: Address) {
        val member = memberRepository.findByUserId(userId)
            ?: throw MemberNotFoundException(userId)
        member.addAddress(address)
        memberRepository.save(member)
    }
}
