package com.padosol.member.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AddressTest : StringSpec({

    "line2 는 선택값(null 허용)이다" {
        val address = Address(zipcode = "12345", line1 = "도로명 1", line2 = null)

        address.zipcode shouldBe "12345"
        address.line1 shouldBe "도로명 1"
        address.line2 shouldBe null
    }
})
