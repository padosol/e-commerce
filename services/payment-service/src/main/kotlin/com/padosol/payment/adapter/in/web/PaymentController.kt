package com.padosol.payment.adapter.`in`.web

import com.padosol.payment.application.port.`in`.ApprovePaymentCommand
import com.padosol.payment.application.port.`in`.PaymentUseCase
import com.padosol.payment.application.port.`in`.PaymentView
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentUseCase: PaymentUseCase,
) {

    @PostMapping
    fun approve(@Valid @RequestBody request: ApprovePaymentRequest): PaymentView =
        paymentUseCase.approve(request.toCommand())

    @PostMapping("/{id}/cancellations")
    fun cancel(@PathVariable id: UUID): PaymentView =
        paymentUseCase.cancel(id)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): PaymentView =
        paymentUseCase.get(id)
}

data class ApprovePaymentRequest(
    @field:NotNull
    val orderId: UUID,
    @field:NotNull
    val amount: BigDecimal,
    @field:NotBlank
    val idempotencyKey: String,
) {
    fun toCommand(): ApprovePaymentCommand = ApprovePaymentCommand(orderId, amount, idempotencyKey)
}
