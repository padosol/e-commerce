package com.padosol.member.application.port.`in`

import java.util.UUID

interface GetMyProfile {
    fun byUserId(userId: UUID): MemberProfile
}
