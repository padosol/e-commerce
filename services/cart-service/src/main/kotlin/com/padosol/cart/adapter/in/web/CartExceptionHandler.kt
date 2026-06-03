package com.padosol.cart.adapter.`in`.web

import com.padosol.cart.application.exception.ProductNotPurchasableException
import com.padosol.cart.domain.model.CartItemNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CartExceptionHandler {

    @ExceptionHandler(CartItemNotFoundException::class)
    fun handleNotFound(e: CartItemNotFoundException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf("code" to "CART_ITEM_NOT_FOUND", "message" to e.message),
        )

    @ExceptionHandler(ProductNotPurchasableException::class)
    fun handleNotPurchasable(e: ProductNotPurchasableException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            mapOf("code" to "PRODUCT_NOT_PURCHASABLE", "message" to e.message),
        )
}
