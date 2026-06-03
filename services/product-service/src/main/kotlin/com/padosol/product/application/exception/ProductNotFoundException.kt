package com.padosol.product.application.exception

import java.util.UUID

class ProductNotFoundException(id: UUID) :
    RuntimeException("상품을 찾을 수 없습니다: $id")
