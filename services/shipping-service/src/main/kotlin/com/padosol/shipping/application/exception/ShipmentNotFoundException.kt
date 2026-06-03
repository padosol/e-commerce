package com.padosol.shipping.application.exception

import java.util.UUID

class ShipmentNotFoundException(id: UUID) :
    RuntimeException("배송 정보를 찾을 수 없습니다: $id")
