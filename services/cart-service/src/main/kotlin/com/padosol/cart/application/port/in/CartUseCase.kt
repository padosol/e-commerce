package com.padosol.cart.application.port.`in`

import java.util.UUID

interface CartUseCase {
    fun addItem(userId: UUID, productId: UUID, quantity: Int): CartView
    fun updateItem(userId: UUID, productId: UUID, quantity: Int): CartView
    fun removeItem(userId: UUID, productId: UUID): CartView
    fun getCart(userId: UUID): CartView
}

data class CartView(
    val userId: UUID,
    val items: List<CartItemView>,
)

data class CartItemView(
    val productId: UUID,
    val quantity: Int,
)
