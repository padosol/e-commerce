package com.padosol.inventory.domain.model

import java.util.UUID

class InsufficientStockException(
    productId: UUID,
    requested: Int,
    available: Int,
) : RuntimeException("재고가 부족합니다(product=$productId, 요청=$requested, 가용=$available)")
