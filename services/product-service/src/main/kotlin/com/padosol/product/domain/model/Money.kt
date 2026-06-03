package com.padosol.product.domain.model

import java.math.BigDecimal

/**
 * 금액 값 객체. 음수 금액은 허용하지 않는다.
 */
data class Money(
    val amount: BigDecimal,
    val currency: String = "KRW",
) {
    init {
        require(amount.signum() >= 0) { "금액은 0 이상이어야 합니다: $amount" }
    }
}
