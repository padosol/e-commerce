package com.padosol.payment.application.service

import com.padosol.payment.application.port.`in`.ApprovePaymentCommand
import com.padosol.payment.application.port.out.PaymentGateway
import com.padosol.payment.application.port.out.PaymentRepository
import com.padosol.payment.application.port.out.PgChargeResult
import com.padosol.payment.domain.model.Payment
import com.padosol.payment.domain.model.PaymentStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID

class PaymentServiceTest : StringSpec({

    val repository = mockk<PaymentRepository>()
    val paymentGateway = mockk<PaymentGateway>()
    val service = PaymentService(repository, paymentGateway)
    val orderId = UUID.randomUUID()

    beforeTest { clearMocks(repository, paymentGateway) }

    "approve: 신규 결제는 PG 승인 후 APPROVED 로 저장된다" {
        every { repository.findByIdempotencyKey("key-1") } returns null
        every { paymentGateway.charge(orderId, BigDecimal("10000")) } returns PgChargeResult(true, "tx-1")
        every { repository.save(any()) } answers { firstArg() }

        val view = service.approve(ApprovePaymentCommand(orderId, BigDecimal("10000"), "key-1"))

        view.status shouldBe PaymentStatus.APPROVED
        view.transactionId shouldBe "tx-1"
    }

    "approve: 동일 멱등키는 기존 결제를 반환하고 PG 를 호출하지 않는다" {
        val existing = Payment.initiate(orderId, BigDecimal("10000"), "key-1").apply { approve("tx-0") }
        every { repository.findByIdempotencyKey("key-1") } returns existing

        val view = service.approve(ApprovePaymentCommand(orderId, BigDecimal("10000"), "key-1"))

        view.transactionId shouldBe "tx-0"
        verify(exactly = 0) { paymentGateway.charge(any(), any()) }
        verify(exactly = 0) { repository.save(any()) }
    }

    "cancel: APPROVED 결제를 취소한다" {
        val payment = Payment.initiate(orderId, BigDecimal("10000"), "key-1").apply { approve("tx-1") }
        every { repository.findById(payment.id) } returns payment
        every { repository.save(any()) } answers { firstArg() }

        val view = service.cancel(payment.id)

        view.status shouldBe PaymentStatus.CANCELLED
    }
})
