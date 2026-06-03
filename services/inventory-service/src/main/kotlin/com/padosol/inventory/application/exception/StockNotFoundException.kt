package com.padosol.inventory.application.exception

import java.util.UUID

class StockNotFoundException(productId: UUID) :
    RuntimeException("재고 정보를 찾을 수 없습니다: $productId")
