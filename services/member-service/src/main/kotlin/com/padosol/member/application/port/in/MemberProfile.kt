package com.padosol.member.application.port.`in`

import com.padosol.member.domain.model.Address
import com.padosol.member.domain.model.MemberGrade
import java.util.UUID

/**
 * 회원 프로필 조회 뷰.
 */
data class MemberProfile(
    val userId: UUID,
    val email: String,
    val nickname: String,
    val grade: MemberGrade,
    val addresses: List<Address>,
)
