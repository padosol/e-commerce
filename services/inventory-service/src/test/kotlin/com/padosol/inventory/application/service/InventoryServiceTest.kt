package com.padosol.inventory.application.service

import com.padosol.inventory.application.exception.StockNotFoundException
import com.padosol.inventory.application.port.out.StockRepository
import com.padosol.inventory.domain.model.Stock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID

class InventoryServiceTest : StringSpec({

    val repository = mockk<StockRepository>()
    val service = InventoryService(repository)
    val productId = UUID.randomUUID()

    beforeTest { clearMocks(repository) }

    "register: 재고를 생성해 저장한다" {
        every { repository.save(any()) } answers { firstArg() }

        val view = service.register(productId, 10)

        view.available shouldBe 10
        view.reserved shouldBe 0
        verify { repository.save(any()) }
    }

    "reserve: 재고가 있으면 예약하고 저장한다" {
        every { repository.findByProductId(productId) } returns Stock.create(productId, 10)
        every { repository.save(any()) } answers { firstArg() }

        val view = service.reserve(productId, 3)

        view.available shouldBe 7
        view.reserved shouldBe 3
    }

    "reserve: 재고가 없으면 StockNotFoundException 을 던진다" {
        every { repository.findByProductId(productId) } returns null

        shouldThrow<StockNotFoundException> {
            service.reserve(productId, 1)
        }
    }
})
