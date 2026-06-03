package com.padosol.inventory.application.service

import com.padosol.inventory.application.exception.StockNotFoundException
import com.padosol.inventory.application.port.`in`.InventoryUseCase
import com.padosol.inventory.application.port.`in`.StockView
import com.padosol.inventory.application.port.out.StockRepository
import com.padosol.inventory.domain.model.Stock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class InventoryService(
    private val stockRepository: StockRepository,
) : InventoryUseCase {

    override fun register(productId: UUID, quantity: Int): StockView =
        stockRepository.save(Stock.create(productId, quantity)).toView()

    override fun reserve(productId: UUID, quantity: Int): StockView =
        mutate(productId) { it.reserve(quantity) }

    override fun confirm(productId: UUID, quantity: Int): StockView =
        mutate(productId) { it.confirm(quantity) }

    override fun release(productId: UUID, quantity: Int): StockView =
        mutate(productId) { it.release(quantity) }

    @Transactional(readOnly = true)
    override fun get(productId: UUID): StockView =
        load(productId).toView()

    private inline fun mutate(productId: UUID, block: (Stock) -> Unit): StockView {
        val stock = load(productId)
        block(stock)
        return stockRepository.save(stock).toView()
    }

    private fun load(productId: UUID): Stock =
        stockRepository.findByProductId(productId) ?: throw StockNotFoundException(productId)

    private fun Stock.toView(): StockView = StockView(productId, available, reserved)
}
