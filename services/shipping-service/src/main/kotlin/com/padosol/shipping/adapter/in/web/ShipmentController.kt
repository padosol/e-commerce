package com.padosol.shipping.adapter.`in`.web

import com.padosol.shipping.application.port.`in`.CreateShipmentCommand
import com.padosol.shipping.application.port.`in`.ShipmentView
import com.padosol.shipping.application.port.`in`.ShippingUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/shipments")
class ShipmentController(
    private val shippingUseCase: ShippingUseCase,
) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateShipmentRequest): ResponseEntity<ShipmentView> =
        ResponseEntity.status(HttpStatus.CREATED).body(
            shippingUseCase.create(
                CreateShipmentCommand(request.orderId, request.zipcode, request.line1, request.line2),
            ),
        )

    @PostMapping("/{id}/dispatch")
    fun dispatch(@PathVariable id: UUID, @Valid @RequestBody request: DispatchRequest): ShipmentView =
        shippingUseCase.ship(id, request.trackingNumber)

    @PostMapping("/{id}/delivery")
    fun deliver(@PathVariable id: UUID): ShipmentView =
        shippingUseCase.deliver(id)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ShipmentView =
        shippingUseCase.get(id)
}

data class CreateShipmentRequest(
    @field:NotNull
    val orderId: UUID,
    @field:NotBlank
    val zipcode: String,
    @field:NotBlank
    val line1: String,
    val line2: String?,
)

data class DispatchRequest(
    @field:NotBlank
    val trackingNumber: String,
)
