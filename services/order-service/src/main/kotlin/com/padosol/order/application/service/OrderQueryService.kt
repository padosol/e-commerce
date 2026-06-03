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

    override fun get(orderId: UUID): OrderView =
        (orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)).toView()
}
