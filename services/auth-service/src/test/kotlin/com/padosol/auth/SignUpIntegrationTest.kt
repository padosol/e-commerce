package com.padosol.auth

import com.padosol.auth.application.port.out.CredentialRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * 통합 테스트(@Tag("integration")) — 실 Postgres/Kafka(Testcontainers) 기반.
 * 기본 `test` 태스크에서 제외되며 `./gradlew :services:auth-service:integrationTest` 로 실행한다.
 * (Docker 가 가능한 환경 필요 — 샌드박스에서는 실행되지 않음)
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SignUpIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var credentialRepository: CredentialRepository

    @Test
    fun `signup 시 자격 증명이 영속화되고 201 을 반환한다`() {
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"itest@example.com","password":"pw12345!"}"""),
        ).andExpect(status().isCreated)

        assertThat(credentialRepository.existsByEmail("itest@example.com")).isTrue()
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
