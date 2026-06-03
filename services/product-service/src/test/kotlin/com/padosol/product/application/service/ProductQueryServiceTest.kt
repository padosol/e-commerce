package com.padosol.product.application.service

import com.padosol.product.application.exception.ProductNotFoundException
import com.padosol.product.application.port.out.ProductRepository
import com.padosol.product.domain.model.Money
import com.padosol.product.domain.model.Product
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.UUID

class ProductQueryServiceTest : StringSpec({

    val repository = mockk<ProductRepository>()
    val service = ProductQueryService(repository)

    "없는 상품 조회 시 ProductNotFoundException 을 던진다" {
        every { repository.findById(any()) } returns null

        shouldThrow<ProductNotFoundException> {
            service.byId(UUID.randomUUID())
        }
    }

    "존재하는 상품은 뷰로 반환한다" {
        val product = Product.register("티셔츠", "설명", Money(BigDecimal("19900")), "의류")
        every { repository.findById(product.id) } returns product

        val view = service.byId(product.id)

        view.name shouldBe "티셔츠"
        view.amount shouldBe BigDecimal("19900")
        view.currency shouldBe "KRW"
        view.category shouldBe "의류"
    }

    "전체 목록을 뷰 리스트로 반환한다" {
        every { repository.findAll() } returns listOf(
            Product.register("A", "", Money(BigDecimal("1000")), "c"),
            Product.register("B", "", Money(BigDecimal("2000")), "c"),
        )

        service.list().size shouldBe 2
    }
})
