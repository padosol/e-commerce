package com.padosol.payment.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.util.UUID

class PaymentTest : StringSpec({

    fun initiate() = Payment.initiate(UUID.randomUUID(), BigDecimal("10000"), "key-1")

    "approve 는 PENDING 을 APPROVED 로 바꾸고 거래번호를 저장한다" {
        val payment = initiate()

        payment.approve("tx-1")

        payment.status shouldBe PaymentStatus.APPROVED
        payment.transactionId shouldBe "tx-1"
    }

    "fail 은 PENDING 을 FAILED 로 바꾼다" {
        val payment = initiate()

        payment.fail()

        payment.status shouldBe PaymentStatus.FAILED
    }

    "cancel 은 APPROVED 결제를 CANCELLED 로 바꾼다" {
        val payment = initiate()
        payment.approve("tx-1")

        payment.cancel()

        payment.status shouldBe PaymentStatus.CANCELLED
    }

    "PENDING 상태는 취소할 수 없다" {
        val payment = initiate()

        shouldThrow<IllegalStateException> {
            payment.cancel()
        }
    }
})
