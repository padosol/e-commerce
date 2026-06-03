package com.padosol.order.application.service

import com.padosol.order.application.port.`in`.OrderLineCommand
import com.padosol.order.application.port.`in`.PlaceOrderCommand
import com.padosol.order.application.port.out.InventoryPort
import com.padosol.order.application.port.out.InventoryReservationException
import com.padosol.order.application.port.out.OrderEventPublisher
import com.padosol.order.application.port.out.OrderRepository
import com.padosol.order.application.port.out.PaymentPort
import com.padosol.order.application.port.out.PaymentResult
import com.padosol.order.application.port.out.ShippingPort
import com.padosol.order.domain.model.Order
import com.padosol.order.domain.model.OrderStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID

class CheckoutServiceTest : StringSpec({

    fun command() = PlaceOrderCommand(
        userId = UUID.randomUUID(),
        lines = listOf(
            OrderLineCommand(UUID.randomUUID(), 2, BigDecimal("10000")),
            OrderLineCommand(UUID.randomUUID(), 1, BigDecimal("5000")),
        ),
        zipcode = "12345",
        line1 = "서울 1로",
        line2 = "201호",
    )

    fun newService(
        inventory: InventoryPort,
        payment: PaymentPort,
        shipping: ShippingPort,
        publisher: OrderEventPublisher,
    ): Pair<CheckoutService, OrderRepository> {
        val repository = mockk<OrderRepository>()
        every { repository.save(any()) } answers { firstArg<Order>() }
        return CheckoutService(repository, inventory, payment, shipping, publisher) to repository
    }

    "정상 플로우: 재고예약→결제승인→재고확정→배송→CONFIRMED" {
        val inventory = mockk<InventoryPort>(relaxed = true)
        val payment = mockk<PaymentPort>()
        val shipping = mockk<ShippingPort>(relaxed = true)
        val publisher = mockk<OrderEventPublisher>(relaxed = true)
        every { payment.approve(any(), any(), any()) } returns PaymentResult(approved = true, paymentId = UUID.randomUUID())
        val (service, _) = newService(inventory, payment, shipping, publisher)

        val view = service.place(command())

        view.status shouldBe OrderStatus.CONFIRMED
        verify(exactly = 2) { inventory.reserve(any(), any()) }
        verify(exactly = 1) { payment.approve(any(), any(), any()) }
        verify(exactly = 2) { inventory.confirm(any(), any()) }
        verify(exactly = 1) { shipping.create(any(), any()) }
        verify(exactly = 1) { publisher.orderConfirmed(any()) }
    }

    "결제 실패: 예약된 재고를 모두 해제하고 CANCELLED" {
        val inventory = mockk<InventoryPort>(relaxed = true)
        val payment = mockk<PaymentPort>()
        val shipping = mockk<ShippingPort>(relaxed = true)
        val publisher = mockk<OrderEventPublisher>(relaxed = true)
        every { payment.approve(any(), any(), any()) } returns PaymentResult(approved = false, paymentId = null)
        val (service, _) = newService(inventory, payment, shipping, publisher)

        val view = service.place(command())

        view.status shouldBe OrderStatus.CANCELLED
        verify(exactly = 2) { inventory.release(any(), any()) }
        verify(exactly = 0) { inventory.confirm(any(), any()) }
        verify(exactly = 0) { shipping.create(any(), any()) }
        verify(exactly = 1) { publisher.orderCancelled(any()) }
    }

    "재고 부족: 결제·배송 미진행, CANCELLED" {
        val inventory = mockk<InventoryPort>(relaxed = true)
        val payment = mockk<PaymentPort>(relaxed = true)
        val shipping = mockk<ShippingPort>(relaxed = true)
        val publisher = mockk<OrderEventPublisher>(relaxed = true)
        every { inventory.reserve(any(), any()) } throws InventoryReservationException(UUID.randomUUID())
        val (service, _) = newService(inventory, payment, shipping, publisher)

        val view = service.place(command())

        view.status shouldBe OrderStatus.CANCELLED
        verify(exactly = 0) { payment.approve(any(), any(), any()) }
        verify(exactly = 0) { shipping.create(any(), any()) }
        verify(exactly = 1) { publisher.orderCancelled(any()) }
    }
})
