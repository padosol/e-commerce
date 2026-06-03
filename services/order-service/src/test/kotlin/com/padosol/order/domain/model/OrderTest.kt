package com.padosol.order.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.util.UUID

class OrderTest : StringSpec({

    fun lines() = listOf(
        OrderLine(UUID.randomUUID(), 2, BigDecimal("10000")),
        OrderLine(UUID.randomUUID(), 1, BigDecimal("5000")),
    )

    "place 는 PENDING 주문을 생성한다" {
        val order = Order.place(UUID.randomUUID(), lines())

        order.status shouldBe OrderStatus.PENDING
        order.lines.size shouldBe 2
    }

    "totalAmount 는 라인 합계(단가×수량)를 더한다" {
        val order = Order.place(UUID.randomUUID(), lines())

        order.totalAmount() shouldBe BigDecimal("25000")
    }

    "confirm 은 PENDING 을 CONFIRMED 로 바꾼다" {
        val order = Order.place(UUID.randomUUID(), lines())

        order.confirm()

        order.status shouldBe OrderStatus.CONFIRMED
    }

    "cancel 은 PENDING 을 CANCELLED 로 바꾼다" {
        val order = Order.place(UUID.randomUUID(), lines())

        order.cancel()

        order.status shouldBe OrderStatus.CANCELLED
    }

    "취소된 주문은 확정할 수 없다" {
        val order = Order.place(UUID.randomUUID(), lines())
        order.cancel()

        shouldThrow<IllegalStateException> {
            order.confirm()
        }
    }
})
