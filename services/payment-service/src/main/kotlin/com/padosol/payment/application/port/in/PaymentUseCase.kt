package com.padosol.payment.application.port.`in`

import com.padosol.payment.domain.model.PaymentStatus
import java.math.BigDecimal
import java.util.UUID

interface PaymentUseCase {
    fun approve(command: ApprovePaymentCommand): PaymentView
    fun cancel(paymentId: UUID): PaymentView
    fun get(paymentId: UUID): PaymentView
}

data class ApprovePaymentCommand(
    val orderId: UUID,
    val amount: BigDecimal,
    val idempotencyKey: String,
)

data class PaymentView(
    val id: UUID,
    val orderId: UUID,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val transactionId: String?,
)
