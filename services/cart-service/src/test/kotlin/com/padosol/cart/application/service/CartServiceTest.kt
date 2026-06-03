package com.padosol.cart.application.service

import com.padosol.cart.application.exception.ProductNotPurchasableException
import com.padosol.cart.application.port.out.CartRepository
import com.padosol.cart.application.port.out.ProductCatalog
import com.padosol.cart.domain.model.Cart
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID

class CartServiceTest : StringSpec({

    val cartRepository = mockk<CartRepository>()
    val productCatalog = mockk<ProductCatalog>()
    val service = CartService(cartRepository, productCatalog)
    val userId = UUID.randomUUID()
    val productId = UUID.randomUUID()

    beforeTest { clearMocks(cartRepository, productCatalog) }

    "addItem: 상품 검증 통과 시 장바구니에 담고 저장한다" {
        every { productCatalog.exists(productId) } returns true
        every { cartRepository.load(userId) } returns null
        every { cartRepository.save(any()) } answers { firstArg() }

        val view = service.addItem(userId, productId, 2)

        view.items.size shouldBe 1
        view.items[0].productId shouldBe productId
        view.items[0].quantity shouldBe 2
        verify { cartRepository.save(any()) }
    }

    "addItem: 구매 불가 상품이면 거부하고 저장하지 않는다" {
        every { productCatalog.exists(productId) } returns false

        shouldThrow<ProductNotPurchasableException> {
            service.addItem(userId, productId, 1)
        }
        verify(exactly = 0) { cartRepository.save(any()) }
    }

    "getCart: 장바구니가 없으면 빈 뷰를 반환한다" {
        every { cartRepository.load(userId) } returns null

        val view = service.getCart(userId)

        view.userId shouldBe userId
        view.items.size shouldBe 0
    }

    "updateItem: 기존 상품 수량을 변경한다" {
        val cart = Cart.empty(userId).apply { addItem(productId, 1) }
        every { cartRepository.load(userId) } returns cart
        every { cartRepository.save(any()) } answers { firstArg() }

        val view = service.updateItem(userId, productId, 5)

        view.items[0].quantity shouldBe 5
    }
})
