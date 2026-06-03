package com.padosol.cart.application.exception

import java.util.UUID

class ProductNotPurchasableException(productId: UUID) :
    RuntimeException("구매할 수 없는 상품입니다: $productId")
