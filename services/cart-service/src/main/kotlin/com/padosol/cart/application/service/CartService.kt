package com.padosol.cart.application.service

import com.padosol.cart.application.exception.ProductNotPurchasableException
import com.padosol.cart.application.port.`in`.CartItemView
import com.padosol.cart.application.port.`in`.CartUseCase
import com.padosol.cart.application.port.`in`.CartView
import com.padosol.cart.application.port.out.CartRepository
import com.padosol.cart.application.port.out.ProductCatalog
import com.padosol.cart.domain.model.Cart
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val productCatalog: ProductCatalog,
) : CartUseCase {

    override fun addItem(userId: UUID, productId: UUID, quantity: Int): CartView {
        if (!productCatalog.exists(productId)) {
            throw ProductNotPurchasableException(productId)
        }
        val cart = cartRepository.load(userId) ?: Cart.empty(userId)
        cart.addItem(productId, quantity)
        return cartRepository.save(cart).toView()
    }

    override fun updateItem(userId: UUID, productId: UUID, quantity: Int): CartView {
        val cart = cartRepository.load(userId) ?: Cart.empty(userId)
        cart.updateQuantity(productId, quantity)
        return cartRepository.save(cart).toView()
    }

    override fun removeItem(userId: UUID, productId: UUID): CartView {
        val cart = cartRepository.load(userId) ?: Cart.empty(userId)
        cart.removeItem(productId)
        return cartRepository.save(cart).toView()
    }

    override fun getCart(userId: UUID): CartView =
        (cartRepository.load(userId) ?: Cart.empty(userId)).toView()

    private fun Cart.toView(): CartView =
        CartView(
            userId = userId,
            items = items.map { CartItemView(it.productId, it.quantity) },
        )
}
