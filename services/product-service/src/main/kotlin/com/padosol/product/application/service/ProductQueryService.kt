package com.padosol.product.application.service

import com.padosol.product.application.exception.ProductNotFoundException
import com.padosol.product.application.port.`in`.ProductQueryUseCase
import com.padosol.product.application.port.`in`.ProductView
import com.padosol.product.application.port.out.ProductRepository
import com.padosol.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ProductQueryService(
    private val productRepository: ProductRepository,
) : ProductQueryUseCase {

    override fun byId(id: UUID): ProductView {
        val product = productRepository.findById(id)
            ?: throw ProductNotFoundException(id)
        return product.toView()
    }

    override fun list(): List<ProductView> =
        productRepository.findAll().map { it.toView() }

    private fun Product.toView(): ProductView =
        ProductView(
            id = id,
            name = name,
            description = description,
            amount = price.amount,
            currency = price.currency,
            category = category,
            status = status,
        )
}
