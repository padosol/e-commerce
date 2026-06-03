package com.padosol.product.adapter.`in`.web

import com.padosol.product.application.exception.ProductNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ProductExceptionHandler {

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleNotFound(e: ProductNotFoundException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf("code" to "PRODUCT_NOT_FOUND", "message" to e.message),
        )
}
