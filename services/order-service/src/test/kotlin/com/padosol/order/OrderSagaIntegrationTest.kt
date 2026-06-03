package com.padosol.order

import com.padosol.order.adapter.out.messaging.OutboxEventJpaRepository
import com.padosol.order.application.port.out.InventoryPort
import com.padosol.order.application.port.out.PaymentPort
import com.padosol.order.application.port.out.PaymentResult
import com.padosol.order.application.port.out.ShippingDestination
import com.padosol.order.application.port.out.ShippingPort
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

/**
 * 체크아웃 Saga 통합 테스트 — 실 Postgres + Kafka(Testcontainers) + Outbox.
 * 다운스트림(inventory/payment/shipping)은 스텁 포트로 대체(@Primary)하여 Saga 오케스트레이션과
 * 보상, Outbox 적재를 검증한다. `./gradlew :services:order-service:integrationTest` 로 실행.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(OrderSagaIntegrationTest.StubConfig::class)
class OrderSagaIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var outboxRepository: OutboxEventJpaRepository
    @Autowired lateinit var paymentStub: PaymentStub
    @Autowired lateinit var inventoryStub: InventoryStub

    @BeforeEach
    fun reset() {
        paymentStub.approve = true
        inventoryStub.releaseCount = 0
    }

    private fun placeOrder(): String {
        val userId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        return mockMvc.perform(
            post("/api/orders")
                .with(jwt().jwt { it.subject(userId.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"lines":[{"productId":"$productId","quantity":2,"unitPrice":10000}],
                       "shippingAddress":{"zipcode":"12345","line1":"서울 1로","line2":null}}""",
                ),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
    }

    @Test
    fun `정상 플로우는 CONFIRMED 이고 Outbox 에 OrderConfirmed 가 적재된다`() {
        paymentStub.approve = true

        val body = placeOrder()

        assertThat(body).contains("CONFIRMED")
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            assertThat(outboxRepository.findAll().any { it.eventType == "OrderConfirmed" }).isTrue()
        }
    }

    @Test
    fun `결제 실패 시 재고를 해제하고 CANCELLED, Outbox 에 OrderCancelled`() {
        paymentStub.approve = false

        val body = placeOrder()

        assertThat(body).contains("CANCELLED")
        assertThat(inventoryStub.releaseCount).isGreaterThan(0) // 보상: 예약 해제
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            assertThat(outboxRepository.findAll().any { it.eventType == "OrderCancelled" }).isTrue()
        }
    }

    @TestConfiguration
    class StubConfig {
        @Bean @Primary fun inventoryStub(): InventoryStub = InventoryStub()
        @Bean @Primary fun paymentStub(): PaymentStub = PaymentStub()
        @Bean @Primary fun shippingStub(): ShippingPort = object : ShippingPort {
            override fun create(orderId: UUID, destination: ShippingDestination) {}
        }
    }

    class InventoryStub : InventoryPort {
        var releaseCount = 0
        override fun reserve(productId: UUID, quantity: Int) {}
        override fun confirm(productId: UUID, quantity: Int) {}
        override fun release(productId: UUID, quantity: Int) {
            releaseCount++
        }
    }

    class PaymentStub : PaymentPort {
        var approve = true
        override fun approve(orderId: UUID, amount: BigDecimal, idempotencyKey: String): PaymentResult =
            PaymentResult(approved = approve, paymentId = if (approve) UUID.randomUUID() else null)
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
