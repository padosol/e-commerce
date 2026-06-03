package com.padosol.cart.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class CartTest : StringSpec({

    "addItem 은 신규 상품을 추가한다" {
        val cart = Cart.empty(UUID.randomUUID())
        val productId = UUID.randomUUID()

        cart.addItem(productId, 2)

        cart.items.size shouldBe 1
        cart.items[0].productId shouldBe productId
        cart.items[0].quantity shouldBe 2
    }

    "addItem 은 동일 상품의 수량을 합친다" {
        val cart = Cart.empty(UUID.randomUUID())
        val productId = UUID.randomUUID()

        cart.addItem(productId, 2)
        cart.addItem(productId, 3)

        cart.items.size shouldBe 1
        cart.items[0].quantity shouldBe 5
    }

    "addItem 은 수량 0 이하를 거부한다" {
        val cart = Cart.empty(UUID.randomUUID())

        shouldThrow<IllegalArgumentException> {
            cart.addItem(UUID.randomUUID(), 0)
        }
    }

    "updateQuantity 는 없는 상품이면 CartItemNotFoundException 을 던진다" {
        val cart = Cart.empty(UUID.randomUUID())

        shouldThrow<CartItemNotFoundException> {
            cart.updateQuantity(UUID.randomUUID(), 1)
        }
    }

    "removeItem 은 상품을 제거한다" {
        val cart = Cart.empty(UUID.randomUUID())
        val productId = UUID.randomUUID()
        cart.addItem(productId, 1)

        cart.removeItem(productId)

        cart.items.size shouldBe 0
    }
})
