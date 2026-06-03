package com.padosol.product.adapter.`in`.web

import com.padosol.product.application.port.`in`.ProductQueryUseCase
import com.padosol.product.application.port.`in`.ProductView
import com.padosol.product.application.port.`in`.RegisterProductCommand
import com.padosol.product.application.port.`in`.RegisterProductUseCase
import com.padosol.product.domain.model.Money
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val registerProductUseCase: RegisterProductUseCase,
    private val productQueryUseCase: ProductQueryUseCase,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<RegisterProductResponse> {
        val productId = registerProductUseCase.register(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterProductResponse(productId))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ProductView = productQueryUseCase.byId(id)

    @GetMapping
    fun list(): List<ProductView> = productQueryUseCase.list()
}

data class RegisterProductRequest(
    @field:NotBlank
    val name: String,
    val description: String = "",
    @field:NotNull
    val amount: BigDecimal,
    val currency: String? = null,
    @field:NotBlank
    val category: String,
) {
    fun toCommand(): RegisterProductCommand =
        RegisterProductCommand(
            name = name,
            description = description,
            price = Money(amount, currency ?: "KRW"),
            category = category,
        )
}

data class RegisterProductResponse(
    val productId: UUID,
)
