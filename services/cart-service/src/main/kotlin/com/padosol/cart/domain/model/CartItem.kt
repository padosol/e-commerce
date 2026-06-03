package com.padosol.cart.domain.model

import java.util.UUID

data class CartItem(
    val productId: UUID,
    val quantity: Int,
) {
    init {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
    }
}
