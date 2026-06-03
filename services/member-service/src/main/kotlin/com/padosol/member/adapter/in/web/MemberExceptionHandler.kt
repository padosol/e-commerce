package com.padosol.member.adapter.`in`.web

import com.padosol.member.application.exception.MemberNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class MemberExceptionHandler {

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(ex: MemberNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                code = "MEMBER_NOT_FOUND",
                message = ex.message ?: "Member not found",
                status = HttpStatus.NOT_FOUND.value(),
            ),
        )

    data class ErrorResponse(
        val code: String,
        val message: String,
        val status: Int,
        val timestamp: Instant = Instant.now(),
    )
}
