package com.padosol.product.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class MoneyTest : StringSpec({

    "음수 금액은 거부한다" {
        shouldThrow<IllegalArgumentException> {
            Money(BigDecimal("-1"))
        }
    }

    "0 이상 금액은 허용하고 기본 통화는 KRW 다" {
        val money = Money(BigDecimal("19900"))

        money.amount shouldBe BigDecimal("19900")
        money.currency shouldBe "KRW"
    }
})
