package com.padosol.member

import com.fasterxml.jackson.databind.ObjectMapper
import com.padosol.common.event.EventTopics
import com.padosol.common.event.UserRegisteredEvent
import com.padosol.member.application.port.out.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.KafkaTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * 통합 테스트(@Tag("integration")) — 실 Postgres/Kafka(Testcontainers) 기반.
 * UserRegistered 이벤트를 실제 Kafka 로 발행하면 consumer 가 회원 프로필을 생성하는지 검증한다.
 * `./gradlew :services:member-service:integrationTest` 로 실행. (Docker 환경 필요)
 */
@Tag("integration")
@SpringBootTest
@Testcontainers
class UserRegisteredConsumerIntegrationTest {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    fun `UserRegistered 이벤트를 소비해 회원 프로필을 생성한다`() {
        val userId = UUID.randomUUID()
        val event = UserRegisteredEvent(userId.toString(), "consumer@example.com", Instant.now())

        kafkaTemplate.send(
            EventTopics.USER_REGISTERED,
            userId.toString(),
            objectMapper.writeValueAsString(event),
        )

        await().atMost(Duration.ofSeconds(20)).untilAsserted {
            assertThat(memberRepository.existsByUserId(userId)).isTrue()
        }
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))

        @Container
        @ServiceConnection
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"))
    }
}
