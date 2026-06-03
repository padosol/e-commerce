package com.padosol.order.application.service

import com.padosol.order.application.exception.OrderNotFoundException
import com.padosol.order.application.port.`in`.GetOrderUseCase
import com.padosol.order.application.port.`in`.OrderView
import com.padosol.order.application.port.out.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OrderQueryService(
    private val orderRepository: OrderRepository,
) : GetOrderUseCase {

    override fun get(orderId: UUID, requesterId: UUID): OrderView {
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        // 소유권 강제(IDOR 방지). 타인 주문은 존재를 노출하지 않도록 NotFound 처리.
        if (order.userId != requesterId) throw OrderNotFoundException(orderId)
        return order.toView()
    }
}
