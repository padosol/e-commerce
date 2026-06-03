package com.padosol.shipping.application.port.`in`

import com.padosol.shipping.domain.model.ShipmentStatus
import java.util.UUID

interface ShippingUseCase {
    fun create(command: CreateShipmentCommand): ShipmentView
    fun ship(shipmentId: UUID, trackingNumber: String): ShipmentView
    fun deliver(shipmentId: UUID): ShipmentView
    fun get(shipmentId: UUID): ShipmentView
}

data class CreateShipmentCommand(
    val orderId: UUID,
    val zipcode: String,
    val line1: String,
    val line2: String?,
)

data class ShipmentView(
    val id: UUID,
    val orderId: UUID,
    val zipcode: String,
    val line1: String,
    val line2: String?,
    val status: ShipmentStatus,
    val trackingNumber: String?,
)
