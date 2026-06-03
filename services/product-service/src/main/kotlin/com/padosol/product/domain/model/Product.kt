package com.padosol.product.domain.model

import java.util.UUID

/**
 * 상품 애그리거트.
 */
class Product(
    val id: UUID,
    val name: String,
    val description: String,
    val price: Money,
    val category: String,
    val status: ProductStatus,
) {
    companion object {
        fun register(name: String, description: String, price: Money, category: String): Product =
            Product(
                id = UUID.randomUUID(),
                name = name,
                description = description,
                price = price,
                category = category,
                status = ProductStatus.ACTIVE,
            )
    }
}
