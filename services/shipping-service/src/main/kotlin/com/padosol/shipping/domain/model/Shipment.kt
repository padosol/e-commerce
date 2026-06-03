package com.padosol.shipping.domain.model

import java.util.UUID

enum class ShipmentStatus { CREATED, IN_TRANSIT, DELIVERED }

data class ShippingAddress(
    val zipcode: String,
    val line1: String,
    val line2: String?,
)

/**
 * 배송 애그리거트. CREATED → IN_TRANSIT(출고) → DELIVERED(완료).
 */
class Shipment(
    val id: UUID,
    val orderId: UUID,
    val address: ShippingAddress,
    status: ShipmentStatus,
    trackingNumber: String?,
) {
    var status: ShipmentStatus = status
        private set
    var trackingNumber: String? = trackingNumber
        private set

    fun ship(trackingNumber: String) {
        check(status == ShipmentStatus.CREATED) { "출고 가능한 상태가 아닙니다: $status" }
        status = ShipmentStatus.IN_TRANSIT
        this.trackingNumber = trackingNumber
    }

    fun deliver() {
        check(status == ShipmentStatus.IN_TRANSIT) { "배송 완료 가능한 상태가 아닙니다: $status" }
        status = ShipmentStatus.DELIVERED
    }

    companion object {
        fun create(orderId: UUID, address: ShippingAddress): Shipment =
            Shipment(UUID.randomUUID(), orderId, address, ShipmentStatus.CREATED, null)
    }
}
