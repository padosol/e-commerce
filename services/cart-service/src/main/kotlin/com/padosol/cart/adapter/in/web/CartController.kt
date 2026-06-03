package com.padosol.cart.adapter.`in`.web

import com.padosol.cart.application.port.`in`.CartUseCase
import com.padosol.cart.application.port.`in`.CartView
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/carts")
class CartController(
    private val cartUseCase: CartUseCase,
) {

    @GetMapping
    fun getCart(@AuthenticationPrincipal jwt: Jwt): CartView =
        cartUseCase.getCart(userId(jwt))

    @PostMapping("/items")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: AddCartItemRequest,
    ): CartView = cartUseCase.addItem(userId(jwt), request.productId, request.quantity)

    @PutMapping("/items/{productId}")
    fun updateItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: UUID,
        @Valid @RequestBody request: UpdateCartItemRequest,
    ): CartView = cartUseCase.updateItem(userId(jwt), productId, request.quantity)

    @DeleteMapping("/items/{productId}")
    fun removeItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: UUID,
    ): CartView = cartUseCase.removeItem(userId(jwt), productId)

    private fun userId(jwt: Jwt): UUID = UUID.fromString(jwt.subject)
}

data class AddCartItemRequest(
    @field:NotNull
    val productId: UUID,
    @field:Min(1)
    val quantity: Int,
)

data class UpdateCartItemRequest(
    @field:Min(1)
    val quantity: Int,
)
