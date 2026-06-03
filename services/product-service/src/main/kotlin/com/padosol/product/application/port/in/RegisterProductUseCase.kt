package com.padosol.product.application.port.`in`

import com.padosol.product.domain.model.Money
import java.util.UUID

interface RegisterProductUseCase {
    fun register(command: RegisterProductCommand): UUID
}

data class RegisterProductCommand(
    val name: String,
    val description: String,
    val price: Money,
    val category: String,
)
