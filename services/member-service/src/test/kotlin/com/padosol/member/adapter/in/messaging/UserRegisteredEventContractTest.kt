package com.padosol.member.adapter.`in`.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.padosol.common.event.UserRegisteredEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.Instant

/**
 * 경량 계약 테스트(이벤트, consumer 측): 합의된 와이어 포맷의 JSON 을 member 가
 * UserRegisteredEvent 로 역직렬화할 수 있는지 보증. auth 측 producer 계약 테스트와 짝을 이룬다.
 */
class UserRegisteredEventContractTest {

    private val objectMapper: ObjectMapper = Jackson2ObjectMapperBuilder().build()

    @Test
    fun `합의된 와이어 스키마를 역직렬화한다`() {
        val contract =
            """{"userId":"11111111-1111-1111-1111-111111111111","email":"user@example.com","occurredAt":"2024-01-01T00:00:00Z"}"""

        val event = objectMapper.readValue(contract, UserRegisteredEvent::class.java)

        assertThat(event.userId).isEqualTo("11111111-1111-1111-1111-111111111111")
        assertThat(event.email).isEqualTo("user@example.com")
        assertThat(event.occurredAt).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"))
    }
}
