package com.padosol.inventory.adapter.`in`.web

import com.padosol.inventory.application.port.`in`.InventoryUseCase
import com.padosol.inventory.application.port.`in`.StockView
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
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
@RequestMapping("/api/inventory")
class InventoryController(
    private val inventoryUseCase: InventoryUseCase,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterStockRequest): ResponseEntity<StockView> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(inventoryUseCase.register(request.productId, request.quantity))

    @PostMapping("/{productId}/reservations")
    fun reserve(@PathVariable productId: UUID, @Valid @RequestBody request: QuantityRequest): StockView =
        inventoryUseCase.reserve(productId, request.quantity)

    @PostMapping("/{productId}/confirmations")
    fun confirm(@PathVariable productId: UUID, @Valid @RequestBody request: QuantityRequest): StockView =
        inventoryUseCase.confirm(productId, request.quantity)

    @PostMapping("/{productId}/releases")
    fun release(@PathVariable productId: UUID, @Valid @RequestBody request: QuantityRequest): StockView =
        inventoryUseCase.release(productId, request.quantity)

    @GetMapping("/{productId}")
    fun get(@PathVariable productId: UUID): StockView =
        inventoryUseCase.get(productId)
}

data class RegisterStockRequest(
    @field:NotNull
    val productId: UUID,
    @field:Min(0)
    val quantity: Int,
)

data class QuantityRequest(
    @field:Min(1)
    val quantity: Int,
)
