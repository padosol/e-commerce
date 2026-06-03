package com.padosol.member.adapter.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.padosol.common.event.EventTopics
import com.padosol.common.event.UserRegisteredEvent
import com.padosol.member.application.port.`in`.RegisterMemberOnUserRegistered
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * UserRegistered 이벤트 구독. 레코드 값은 JSON String이므로 ObjectMapper로 역직렬화한다.
 * 멱등 처리는 RegisterMemberOnUserRegistered(서비스)가 책임지므로 중복 전달에 안전하다.
 */
@Component
class UserRegisteredKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val registerMember: RegisterMemberOnUserRegistered,
) {

    @KafkaListener(topics = [EventTopics.USER_REGISTERED], groupId = "member-service")
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, UserRegisteredEvent::class.java)
        registerMember.handle(UUID.fromString(event.userId), event.email)
    }
}
