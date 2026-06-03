package com.padosol.shipping.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ShipmentTest : StringSpec({

    fun create() = Shipment.create(UUID.randomUUID(), ShippingAddress("12345", "서울 1로", "201호"))

    "create 는 CREATED 상태로 배송을 생성한다" {
        val shipment = create()

        shipment.status shouldBe ShipmentStatus.CREATED
        shipment.trackingNumber shouldBe null
    }

    "ship 은 CREATED 를 IN_TRANSIT 로 바꾸고 운송장 번호를 부여한다" {
        val shipment = create()

        shipment.ship("TRACK-1")

        shipment.status shouldBe ShipmentStatus.IN_TRANSIT
        shipment.trackingNumber shouldBe "TRACK-1"
    }

    "deliver 는 IN_TRANSIT 를 DELIVERED 로 바꾼다" {
        val shipment = create()
        shipment.ship("TRACK-1")

        shipment.deliver()

        shipment.status shouldBe ShipmentStatus.DELIVERED
    }

    "출고 전에는 배송 완료할 수 없다" {
        val shipment = create()

        shouldThrow<IllegalStateException> {
            shipment.deliver()
        }
    }
})
