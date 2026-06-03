package com.padosol.shipping.adapter.`in`.web

import com.padosol.shipping.application.exception.ShipmentNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ShipmentExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException::class)
    fun handleNotFound(e: ShipmentNotFoundException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            mapOf("code" to "SHIPMENT_NOT_FOUND", "message" to e.message),
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf("code" to "INVALID_SHIPMENT_STATE", "message" to e.message),
        )
}
