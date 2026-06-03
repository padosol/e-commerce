package com.padosol.inventory.domain.model

import java.util.UUID

/**
 * 재고 애그리거트. available(가용) / reserved(예약)로 상태를 관리한다.
 *  - reserve: 가용 → 예약 (주문 시 홀드)
 *  - confirm: 예약 차감 (배송 확정 등 최종 소진)
 *  - release: 예약 → 가용 (주문 취소/보상)
 */
class Stock(
    val productId: UUID,
    available: Int,
    reserved: Int,
) {
    var available: Int = available
        private set
    var reserved: Int = reserved
        private set

    init {
        require(available >= 0) { "가용 재고는 0 이상이어야 합니다: $available" }
        require(reserved >= 0) { "예약 재고는 0 이상이어야 합니다: $reserved" }
    }

    fun reserve(quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        if (available < quantity) throw InsufficientStockException(productId, quantity, available)
        available -= quantity
        reserved += quantity
    }

    fun confirm(quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        check(reserved >= quantity) { "확정할 예약 수량이 부족합니다: $quantity > $reserved" }
        reserved -= quantity
    }

    fun release(quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        check(reserved >= quantity) { "해제할 예약 수량이 부족합니다: $quantity > $reserved" }
        reserved -= quantity
        available += quantity
    }

    companion object {
        fun create(productId: UUID, quantity: Int): Stock {
            require(quantity >= 0) { "초기 재고는 0 이상이어야 합니다: $quantity" }
            return Stock(productId, quantity, 0)
        }
    }
}
