package com.padosol.inventory.application.port.`in`

import java.util.UUID

interface InventoryUseCase {
    fun register(productId: UUID, quantity: Int): StockView
    fun reserve(productId: UUID, quantity: Int): StockView
    fun confirm(productId: UUID, quantity: Int): StockView
    fun release(productId: UUID, quantity: Int): StockView
    fun get(productId: UUID): StockView
}

data class StockView(
    val productId: UUID,
    val available: Int,
    val reserved: Int,
)
