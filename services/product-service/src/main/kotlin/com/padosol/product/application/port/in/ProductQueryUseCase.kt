package com.padosol.product.application.port.`in`

import com.padosol.product.domain.model.ProductStatus
import java.math.BigDecimal
import java.util.UUID

interface ProductQueryUseCase {
    fun byId(id: UUID): ProductView
    fun list(): List<ProductView>
}

/**
 * 상품 조회 뷰.
 */
data class ProductView(
    val id: UUID,
    val name: String,
    val description: String,
    val amount: BigDecimal,
    val currency: String,
    val category: String,
    val status: ProductStatus,
)
