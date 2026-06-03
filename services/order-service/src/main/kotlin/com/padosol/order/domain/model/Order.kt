package com.padosol.order.domain.model

import java.math.BigDecimal
import java.util.UUID

enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }

data class OrderLine(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
) {
    init {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        require(unitPrice.signum() >= 0) { "단가는 0 이상이어야 합니다: $unitPrice" }
    }

    fun lineTotal(): BigDecimal = unitPrice.multiply(BigDecimal(quantity))
}

/**
 * 주문 애그리거트. PENDING → CONFIRMED | CANCELLED.
 */
class Order(
    val id: UUID,
    val userId: UUID,
    val lines: List<OrderLine>,
    status: OrderStatus,
) {
    var status: OrderStatus = status
        private set

    init {
        require(lines.isNotEmpty()) { "주문에는 1개 이상의 라인이 필요합니다" }
    }

    fun totalAmount(): BigDecimal =
        lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.lineTotal()) }

    fun confirm() {
        check(status == OrderStatus.PENDING) { "확정 가능한 상태가 아닙니다: $status" }
        status = OrderStatus.CONFIRMED
    }

    fun cancel() {
        check(status == OrderStatus.PENDING) { "취소 가능한 상태가 아닙니다: $status" }
        status = OrderStatus.CANCELLED
    }

    companion object {
        fun place(userId: UUID, lines: List<OrderLine>): Order =
            Order(UUID.randomUUID(), userId, lines, OrderStatus.PENDING)
    }
}
