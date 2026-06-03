package com.padosol.product.application.service

import com.padosol.product.application.port.`in`.RegisterProductCommand
import com.padosol.product.application.port.out.ProductRepository
import com.padosol.product.domain.model.Money
import com.padosol.product.domain.model.Product
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal

class ProductRegistrationServiceTest : StringSpec({

    val repository = mockk<ProductRepository>()
    val service = ProductRegistrationService(repository)

    "상품을 저장하고 생성된 id 를 반환한다" {
        val saved = slot<Product>()
        every { repository.save(capture(saved)) } answers { saved.captured }

        val id = service.register(
            RegisterProductCommand(
                name = "베이직 티셔츠",
                description = "면 100%",
                price = Money(BigDecimal("19900")),
                category = "의류",
            ),
        )

        id shouldBe saved.captured.id
        saved.captured.name shouldBe "베이직 티셔츠"
        verify(exactly = 1) { repository.save(any()) }
    }
})
