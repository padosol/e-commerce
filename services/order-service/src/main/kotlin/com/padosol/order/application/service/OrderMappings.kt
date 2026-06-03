package com.padosol.order.application.service

import com.padosol.order.application.port.`in`.OrderLineView
import com.padosol.order.application.port.`in`.OrderView
import com.padosol.order.domain.model.Order

internal fun Order.toView(): OrderView =
    OrderView(
        id = id,
        userId = userId,
        status = status,
        totalAmount = totalAmount(),
        lines = lines.map { OrderLineView(it.productId, it.quantity, it.unitPrice) },
    )
