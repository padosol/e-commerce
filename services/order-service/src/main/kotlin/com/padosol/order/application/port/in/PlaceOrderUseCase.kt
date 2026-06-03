package com.padosol.order.application.port.`in`

import com.padosol.order.domain.model.OrderStatus
import java.math.BigDecimal
import java.util.UUID

interface PlaceOrderUseCase {
    fun place(command: PlaceOrderCommand): OrderView
}

interface GetOrderUseCase {
    fun get(orderId: UUID): OrderView
}

data class PlaceOrderCommand(
    val userId: UUID,
    val lines: List<OrderLineCommand>,
    val zipcode: String,
    val line1: String,
    val line2: String?,
)

data class OrderLineCommand(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
)

data class OrderView(
    val id: UUID,
    val userId: UUID,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val lines: List<OrderLineView>,
)

data class OrderLineView(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
)
