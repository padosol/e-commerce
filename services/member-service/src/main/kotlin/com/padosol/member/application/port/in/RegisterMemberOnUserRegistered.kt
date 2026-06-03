package com.padosol.member.application.port.`in`

import java.util.UUID

/**
 * UserRegistered 이벤트를 받아 회원 프로필을 생성한다. 중복 전달에 대해 멱등하다.
 */
interface RegisterMemberOnUserRegistered {
    fun handle(userId: UUID, email: String)
}
