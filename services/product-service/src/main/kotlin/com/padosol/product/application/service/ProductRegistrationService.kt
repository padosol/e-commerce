package com.padosol.product.application.service

import com.padosol.product.application.port.`in`.RegisterProductCommand
import com.padosol.product.application.port.`in`.RegisterProductUseCase
import com.padosol.product.application.port.out.ProductRepository
import com.padosol.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductRegistrationService(
    private val productRepository: ProductRepository,
) : RegisterProductUseCase {

    @Transactional
    override fun register(command: RegisterProductCommand): UUID {
        val product = productRepository.save(
            Product.register(
                name = command.name,
                description = command.description,
                price = command.price,
                category = command.category,
            ),
        )
        return product.id
    }
}
