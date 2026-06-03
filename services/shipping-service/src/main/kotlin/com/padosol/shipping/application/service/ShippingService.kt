package com.padosol.shipping.application.service

import com.padosol.shipping.application.exception.ShipmentNotFoundException
import com.padosol.shipping.application.port.`in`.CreateShipmentCommand
import com.padosol.shipping.application.port.`in`.ShipmentView
import com.padosol.shipping.application.port.`in`.ShippingUseCase
import com.padosol.shipping.application.port.out.ShipmentRepository
import com.padosol.shipping.domain.model.Shipment
import com.padosol.shipping.domain.model.ShippingAddress
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ShippingService(
    private val shipmentRepository: ShipmentRepository,
) : ShippingUseCase {

    override fun create(command: CreateShipmentCommand): ShipmentView {
        val shipment = Shipment.create(
            command.orderId,
            ShippingAddress(command.zipcode, command.line1, command.line2),
        )
        return shipmentRepository.save(shipment).toView()
    }

    override fun ship(shipmentId: UUID, trackingNumber: String): ShipmentView {
        val shipment = load(shipmentId)
        shipment.ship(trackingNumber)
        return shipmentRepository.save(shipment).toView()
    }

    override fun deliver(shipmentId: UUID): ShipmentView {
        val shipment = load(shipmentId)
        shipment.deliver()
        return shipmentRepository.save(shipment).toView()
    }

    @Transactional(readOnly = true)
    override fun get(shipmentId: UUID): ShipmentView = load(shipmentId).toView()

    private fun load(shipmentId: UUID): Shipment =
        shipmentRepository.findById(shipmentId) ?: throw ShipmentNotFoundException(shipmentId)

    private fun Shipment.toView(): ShipmentView =
        ShipmentView(
            id = id,
            orderId = orderId,
            zipcode = address.zipcode,
            line1 = address.line1,
            line2 = address.line2,
            status = status,
            trackingNumber = trackingNumber,
        )
}
