package com.padosol.cart.domain.model

import java.util.UUID

/**
 * 장바구니 애그리거트. 사용자(userId)별 1개.
 */
class Cart(
    val userId: UUID,
    val items: MutableList<CartItem> = mutableListOf(),
) {
    fun addItem(productId: UUID, quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        val index = items.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            items[index] = CartItem(productId, items[index].quantity + quantity)
        } else {
            items.add(CartItem(productId, quantity))
        }
    }

    fun updateQuantity(productId: UUID, quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다: $quantity" }
        val index = items.indexOfFirst { it.productId == productId }
        if (index < 0) throw CartItemNotFoundException(productId)
        items[index] = CartItem(productId, quantity)
    }

    fun removeItem(productId: UUID) {
        items.removeIf { it.productId == productId }
    }

    companion object {
        fun empty(userId: UUID): Cart = Cart(userId, mutableListOf())
    }
}
