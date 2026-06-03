package com.padosol.inventory.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class StockTest : StringSpec({

    val productId = UUID.randomUUID()

    "reserve 는 가용을 줄이고 예약을 늘린다" {
        val stock = Stock.create(productId, 10)

        stock.reserve(3)

        stock.available shouldBe 7
        stock.reserved shouldBe 3
    }

    "reserve 는 가용 재고가 부족하면 InsufficientStockException 을 던진다" {
        val stock = Stock.create(productId, 2)

        shouldThrow<InsufficientStockException> {
            stock.reserve(5)
        }
    }

    "confirm 은 예약 수량을 차감한다(가용은 그대로)" {
        val stock = Stock.create(productId, 10)
        stock.reserve(3)

        stock.confirm(2)

        stock.available shouldBe 7
        stock.reserved shouldBe 1
    }

    "release 는 예약을 가용으로 되돌린다" {
        val stock = Stock.create(productId, 10)
        stock.reserve(3)

        stock.release(3)

        stock.available shouldBe 10
        stock.reserved shouldBe 0
    }
})
