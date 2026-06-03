package com.padosol.common.event

import java.time.Instant

/**
 * 회원가입 완료 시 auth-service가 발행하고 member-service가 구독한다.
 * JSON(String)으로 직렬화되어 토픽 [EventTopics.USER_REGISTERED]로 전송된다.
 */
data class UserRegisteredEvent(
    val userId: String,
    val email: String,
    val occurredAt: Instant,
)
