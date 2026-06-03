package com.padosol.product.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.product.application.port.`in`.ProductView
import com.padosol.product.application.port.`in`.RegisterProductUseCase
import com.padosol.product.application.port.`in`.ProductQueryUseCase
import com.padosol.product.domain.model.ProductStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(RestDocumentationExtension::class)
class ProductControllerDocsTest {

    private val registerProductUseCase = mockk<RegisterProductUseCase>()
    private val productQueryUseCase = mockk<ProductQueryUseCase>()
    private val productId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(ProductController(registerProductUseCase, productQueryUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun sampleView() = ProductView(
        id = productId,
        name = "베이직 티셔츠",
        description = "면 100%",
        amount = BigDecimal("19900"),
        currency = "KRW",
        category = "의류",
        status = ProductStatus.ACTIVE,
    )

    @Test
    fun `상품 등록 API 문서화`() {
        every { registerProductUseCase.register(any()) } returns productId

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"베이직 티셔츠","description":"면 100%","amount":19900,"currency":"KRW","category":"의류"}"""),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "product-register",
                    requestFields(
                        fieldWithPath("name").description("상품명"),
                        fieldWithPath("description").description("상품 설명"),
                        fieldWithPath("amount").description("가격(금액)"),
                        fieldWithPath("currency").description("통화(기본 KRW)").optional(),
                        fieldWithPath("category").description("카테고리"),
                    ),
                    responseFields(
                        fieldWithPath("productId").description("생성된 상품 ID"),
                    ),
                ),
            )
    }

    @Test
    fun `상품 단건 조회 API 문서화`() {
        every { productQueryUseCase.byId(productId) } returns sampleView()

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "product-get",
                    responseFields(
                        fieldWithPath("id").description("상품 ID"),
                        fieldWithPath("name").description("상품명"),
                        fieldWithPath("description").description("상품 설명"),
                        fieldWithPath("amount").description("가격(금액)"),
                        fieldWithPath("currency").description("통화"),
                        fieldWithPath("category").description("카테고리"),
                        fieldWithPath("status").description("상태(ACTIVE/INACTIVE)"),
                    ),
                ),
            )
    }

    @Test
    fun `상품 목록 조회 API 문서화`() {
        every { productQueryUseCase.list() } returns listOf(sampleView())

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "product-list",
                    responseFields(
                        fieldWithPath("[]").description("상품 목록"),
                        fieldWithPath("[].id").description("상품 ID"),
                        fieldWithPath("[].name").description("상품명"),
                        fieldWithPath("[].description").description("상품 설명"),
                        fieldWithPath("[].amount").description("가격(금액)"),
                        fieldWithPath("[].currency").description("통화"),
                        fieldWithPath("[].category").description("카테고리"),
                        fieldWithPath("[].status").description("상태"),
                    ),
                ),
            )
    }
}
