package com.padosol.order.application.service

import com.padosol.order.application.exception.OrderNotFoundException
import com.padosol.order.application.port.out.OrderRepository
import com.padosol.order.domain.model.Order
import com.padosol.order.domain.model.OrderLine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.UUID

class OrderQueryServiceTest : StringSpec({

    val repository = mockk<OrderRepository>()
    val service = OrderQueryService(repository)

    fun order(owner: UUID) =
        Order.place(owner, listOf(OrderLine(UUID.randomUUID(), 1, BigDecimal("1000"))))

    "본인 주문은 조회된다" {
        val owner = UUID.randomUUID()
        val order = order(owner)
        every { repository.findById(order.id) } returns order

        service.get(order.id, owner).userId shouldBe owner
    }

    "타인 주문 조회 시 OrderNotFoundException 으로 차단한다(IDOR 방지)" {
        val order = order(UUID.randomUUID())
        every { repository.findById(order.id) } returns order

        shouldThrow<OrderNotFoundException> {
            service.get(order.id, UUID.randomUUID())
        }
    }

    "없는 주문은 OrderNotFoundException" {
        every { repository.findById(any()) } returns null

        shouldThrow<OrderNotFoundException> {
            service.get(UUID.randomUUID(), UUID.randomUUID())
        }
    }
})
