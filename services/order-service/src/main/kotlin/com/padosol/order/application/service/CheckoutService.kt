package com.padosol.order.application.service

import com.padosol.order.application.port.`in`.OrderView
import com.padosol.order.application.port.`in`.PlaceOrderCommand
import com.padosol.order.application.port.`in`.PlaceOrderUseCase
import com.padosol.order.application.port.out.InventoryPort
import com.padosol.order.application.port.out.InventoryReservationException
import com.padosol.order.application.port.out.OrderEventPublisher
import com.padosol.order.application.port.out.OrderRepository
import com.padosol.order.application.port.out.PaymentPort
import com.padosol.order.application.port.out.ShippingDestination
import com.padosol.order.application.port.out.ShippingPort
import com.padosol.order.domain.model.Order
import com.padosol.order.domain.model.OrderLine
import org.springframework.stereotype.Service

/**
 * 체크아웃 오케스트레이션 Saga: 재고예약 → 결제 → 재고확정 → 배송 → CONFIRMED.
 * 단계 실패 시 보상(예약 해제) 후 주문 CANCELLED.
 *
 * NOTE: 외부 동기 호출이 포함되므로, 운영에선 주문 상태/Outbox 기록만 짧은 트랜잭션으로 분리하고
 *       단계 간 멱등/타임아웃/재시도를 보강하는 것이 바람직하다(Phase 5).
 */
@Service
class CheckoutService(
    private val orderRepository: OrderRepository,
    private val inventoryPort: InventoryPort,
    private val paymentPort: PaymentPort,
    private val shippingPort: ShippingPort,
    private val orderEventPublisher: OrderEventPublisher,
) : PlaceOrderUseCase {

    override fun place(command: PlaceOrderCommand): OrderView {
        val order = Order.place(
            command.userId,
            command.lines.map { OrderLine(it.productId, it.quantity, it.unitPrice) },
        )
        orderRepository.save(order)

        // 1. 재고 예약 (실패 시 이미 예약한 라인 보상)
        val reserved = mutableListOf<OrderLine>()
        try {
            order.lines.forEach {
                inventoryPort.reserve(it.productId, it.quantity)
                reserved.add(it)
            }
        } catch (e: InventoryReservationException) {
            releaseAll(reserved)
            return cancel(order)
        }

        // 2. 결제 (실패 시 재고 해제)
        val payment = paymentPort.approve(order.id, order.totalAmount(), order.id.toString())
        if (!payment.approved) {
            releaseAll(reserved)
            return cancel(order)
        }

        // 3. 재고 확정 + 4. 배송 생성
        order.lines.forEach { inventoryPort.confirm(it.productId, it.quantity) }
        shippingPort.create(order.id, ShippingDestination(command.zipcode, command.line1, command.line2))

        // 5. 주문 확정
        order.confirm()
        orderRepository.save(order)
        orderEventPublisher.orderConfirmed(order.id)
        return order.toView()
    }

    private fun cancel(order: Order): OrderView {
        order.cancel()
        orderRepository.save(order)
        orderEventPublisher.orderCancelled(order.id)
        return order.toView()
    }

    private fun releaseAll(reserved: List<OrderLine>) {
        reserved.forEach { inventoryPort.release(it.productId, it.quantity) }
    }
}
