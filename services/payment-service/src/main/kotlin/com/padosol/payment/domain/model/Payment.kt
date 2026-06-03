package com.padosol.payment.domain.model

import java.math.BigDecimal
import java.util.UUID

enum class PaymentStatus { PENDING, APPROVED, FAILED, CANCELLED }

/**
 * 결제 애그리거트. PENDING → (APPROVED | FAILED), APPROVED → CANCELLED.
 */
class Payment(
    val id: UUID,
    val orderId: UUID,
    val amount: BigDecimal,
    val idempotencyKey: String,
    status: PaymentStatus,
    transactionId: String?,
) {
    var status: PaymentStatus = status
        private set
    var transactionId: String? = transactionId
        private set

    init {
        require(amount.signum() > 0) { "결제 금액은 0보다 커야 합니다: $amount" }
    }

    fun approve(transactionId: String) {
        check(status == PaymentStatus.PENDING) { "승인 가능한 상태가 아닙니다: $status" }
        status = PaymentStatus.APPROVED
        this.transactionId = transactionId
    }

    fun fail() {
        check(status == PaymentStatus.PENDING) { "실패 처리 가능한 상태가 아닙니다: $status" }
        status = PaymentStatus.FAILED
    }

    fun cancel() {
        check(status == PaymentStatus.APPROVED) { "취소 가능한 상태가 아닙니다: $status" }
        status = PaymentStatus.CANCELLED
    }

    companion object {
        fun initiate(orderId: UUID, amount: BigDecimal, idempotencyKey: String): Payment =
            Payment(UUID.randomUUID(), orderId, amount, idempotencyKey, PaymentStatus.PENDING, null)
    }
}
