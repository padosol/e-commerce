package com.padosol.payment.application.exception

import java.util.UUID

class PaymentNotFoundException(id: UUID) :
    RuntimeException("결제를 찾을 수 없습니다: $id")
