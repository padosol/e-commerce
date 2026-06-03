package com.padosol.member.domain.model

import java.util.UUID

/**
 * 회원 애그리거트. auth-service의 사용자(userId)와 1:1로 연결된다.
 * id는 등록 시점에 생성되는 별도 식별자이며, userId는 인증 정체성을 가리킨다.
 */
class Member(
    val id: UUID,
    val userId: UUID,
    val email: String,
    val nickname: String,
    val grade: MemberGrade,
    val addresses: MutableList<Address>,
) {
    fun addAddress(address: Address) {
        addresses.add(address)
    }

    companion object {
        fun register(userId: UUID, email: String): Member {
            val nickname = email.substringBefore('@')
            return Member(
                id = UUID.randomUUID(),
                userId = userId,
                email = email,
                nickname = nickname,
                grade = MemberGrade.BRONZE,
                addresses = mutableListOf(),
            )
        }
    }
}
