package com.padosol.shipping.application.service

import com.padosol.shipping.application.exception.ShipmentNotFoundException
import com.padosol.shipping.application.port.`in`.CreateShipmentCommand
import com.padosol.shipping.application.port.out.ShipmentRepository
import com.padosol.shipping.domain.model.Shipment
import com.padosol.shipping.domain.model.ShipmentStatus
import com.padosol.shipping.domain.model.ShippingAddress
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class ShippingServiceTest : StringSpec({

    val repository = mockk<ShipmentRepository>()
    val service = ShippingService(repository)
    val orderId = UUID.randomUUID()

    beforeTest { clearMocks(repository) }

    "create: 배송을 생성해 저장한다" {
        every { repository.save(any()) } answers { firstArg() }

        val view = service.create(CreateShipmentCommand(orderId, "12345", "서울 1로", "201호"))

        view.status shouldBe ShipmentStatus.CREATED
        view.orderId shouldBe orderId
    }

    "ship: 배송을 출고 처리한다" {
        val shipment = Shipment.create(orderId, ShippingAddress("12345", "서울 1로", null))
        every { repository.findById(shipment.id) } returns shipment
        every { repository.save(any()) } answers { firstArg() }

        val view = service.ship(shipment.id, "TRACK-1")

        view.status shouldBe ShipmentStatus.IN_TRANSIT
        view.trackingNumber shouldBe "TRACK-1"
    }

    "get: 배송이 없으면 ShipmentNotFoundException 을 던진다" {
        every { repository.findById(any()) } returns null

        shouldThrow<ShipmentNotFoundException> {
            service.get(UUID.randomUUID())
        }
    }
})
