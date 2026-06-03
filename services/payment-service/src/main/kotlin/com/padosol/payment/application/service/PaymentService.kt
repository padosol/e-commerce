package com.padosol.payment.application.service

import com.padosol.payment.application.exception.PaymentNotFoundException
import com.padosol.payment.application.port.`in`.ApprovePaymentCommand
import com.padosol.payment.application.port.`in`.PaymentUseCase
import com.padosol.payment.application.port.`in`.PaymentView
import com.padosol.payment.application.port.out.PaymentGateway
import com.padosol.payment.application.port.out.PaymentRepository
import com.padosol.payment.domain.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
) : PaymentUseCase {

    override fun approve(command: ApprovePaymentCommand): PaymentView {
        // 멱등: 동일 키의 결제가 있으면 PG 재호출 없이 기존 결과 반환
        paymentRepository.findByIdempotencyKey(command.idempotencyKey)?.let {
            return it.toView()
        }

        val payment = Payment.initiate(command.orderId, command.amount, command.idempotencyKey)
        val result = paymentGateway.charge(command.orderId, command.amount)
        if (result.approved) {
            payment.approve(requireNotNull(result.transactionId) { "승인 시 거래번호가 필요합니다" })
        } else {
            payment.fail()
        }
        return paymentRepository.save(payment).toView()
    }

    override fun cancel(paymentId: UUID): PaymentView {
        val payment = load(paymentId)
        payment.cancel()
        return paymentRepository.save(payment).toView()
    }

    @Transactional(readOnly = true)
    override fun get(paymentId: UUID): PaymentView = load(paymentId).toView()

    private fun load(paymentId: UUID): Payment =
        paymentRepository.findById(paymentId) ?: throw PaymentNotFoundException(paymentId)

    private fun Payment.toView(): PaymentView =
        PaymentView(id, orderId, amount, status, transactionId)
}
