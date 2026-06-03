package com.padosol.inventory.adapter.`in`.web

import com.padosol.inventory.application.exception.StockNotFoundException
import com.padosol.inventory.domain.model.InsufficientStockException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class InventoryExceptionHandler {

    @ExceptionHandler(StockNotFoundException::class)
    fun handleNotFound(e: StockNotFoundException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf("code" to "STOCK_NOT_FOUND", "message" to e.message),
        )

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficient(e: InsufficientStockException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf("code" to "INSUFFICIENT_STOCK", "message" to e.message),
        )
}
