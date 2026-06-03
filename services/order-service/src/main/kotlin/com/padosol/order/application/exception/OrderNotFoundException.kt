package com.padosol.order.application.exception

import java.util.UUID

class OrderNotFoundException(id: UUID) :
    RuntimeException("주문을 찾을 수 없습니다: $id")
