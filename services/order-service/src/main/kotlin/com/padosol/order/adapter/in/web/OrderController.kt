package com.padosol.order.adapter.`in`.web

import com.padosol.order.application.port.`in`.GetOrderUseCase
import com.padosol.order.application.port.`in`.OrderLineCommand
import com.padosol.order.application.port.`in`.OrderView
import com.padosol.order.application.port.`in`.PlaceOrderCommand
import com.padosol.order.application.port.`in`.PlaceOrderUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getOrderUseCase: GetOrderUseCase,
) {

    @PostMapping
    fun place(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: PlaceOrderRequest,
    ): ResponseEntity<OrderView> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(placeOrderUseCase.place(request.toCommand(UUID.fromString(jwt.subject))))

    @GetMapping("/{id}")
    fun get(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID): OrderView =
        getOrderUseCase.get(id, UUID.fromString(jwt.subject))
}

data class PlaceOrderRequest(
    @field:NotEmpty
    @field:Valid
    val lines: List<OrderLineRequest>,
    @field:NotNull
    @field:Valid
    val shippingAddress: ShippingAddressRequest,
) {
    fun toCommand(userId: UUID): PlaceOrderCommand =
        PlaceOrderCommand(
            userId = userId,
            lines = lines.map { OrderLineCommand(it.productId, it.quantity, it.unitPrice) },
            zipcode = shippingAddress.zipcode,
            line1 = shippingAddress.line1,
            line2 = shippingAddress.line2,
        )
}

data class OrderLineRequest(
    @field:NotNull
    val productId: UUID,
    @field:Min(1)
    val quantity: Int,
    @field:NotNull
    val unitPrice: BigDecimal,
)

data class ShippingAddressRequest(
    @field:NotBlank
    val zipcode: String,
    @field:NotBlank
    val line1: String,
    val line2: String?,
)
