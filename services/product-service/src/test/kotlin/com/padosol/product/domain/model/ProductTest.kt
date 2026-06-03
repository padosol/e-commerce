package com.padosol.product.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class ProductTest : StringSpec({

    "register 는 ACTIVE 상태의 상품을 생성한다" {
        val product = Product.register(
            name = "베이직 티셔츠",
            description = "면 100%",
            price = Money(BigDecimal("19900")),
            category = "의류",
        )

        product.name shouldBe "베이직 티셔츠"
        product.description shouldBe "면 100%"
        product.price.amount shouldBe BigDecimal("19900")
        product.category shouldBe "의류"
        product.status shouldBe ProductStatus.ACTIVE
    }
})
