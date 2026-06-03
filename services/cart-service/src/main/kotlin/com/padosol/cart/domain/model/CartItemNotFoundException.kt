package com.padosol.cart.domain.model

import java.util.UUID

class CartItemNotFoundException(productId: UUID) :
    RuntimeException("장바구니에 해당 상품이 없습니다: $productId")
