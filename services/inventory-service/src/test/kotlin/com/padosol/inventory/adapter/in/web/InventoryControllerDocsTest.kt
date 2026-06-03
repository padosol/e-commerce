package com.padosol.inventory.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.inventory.application.port.`in`.InventoryUseCase
import com.padosol.inventory.application.port.`in`.StockView
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
import java.util.UUID

@ExtendWith(RestDocumentationExtension::class)
class InventoryControllerDocsTest {

    private val inventoryUseCase = mockk<InventoryUseCase>()
    private val productId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(InventoryController(inventoryUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    @Test
    fun `재고 등록 API 문서화`() {
        every { inventoryUseCase.register(productId, 100) } returns StockView(productId, 100, 0)

        mockMvc.perform(
            post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"productId":"$productId","quantity":100}"""),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "inventory-register",
                    requestFields(
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("quantity").description("초기 재고 수량"),
                    ),
                    responseFields(
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("available").description("가용 재고"),
                        fieldWithPath("reserved").description("예약 재고"),
                    ),
                ),
            )
    }

    @Test
    fun `재고 예약 API 문서화`() {
        every { inventoryUseCase.reserve(productId, 3) } returns StockView(productId, 97, 3)

        mockMvc.perform(
            post("/api/inventory/{productId}/reservations", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"quantity":3}"""),
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "inventory-reserve",
                    requestFields(
                        fieldWithPath("quantity").description("예약 수량"),
                    ),
                    responseFields(
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("available").description("가용 재고"),
                        fieldWithPath("reserved").description("예약 재고"),
                    ),
                ),
            )
    }

    @Test
    fun `재고 조회 API 문서화`() {
        every { inventoryUseCase.get(productId) } returns StockView(productId, 97, 3)

        mockMvc.perform(get("/api/inventory/{productId}", productId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "inventory-get",
                    responseFields(
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("available").description("가용 재고"),
                        fieldWithPath("reserved").description("예약 재고"),
                    ),
                ),
            )
    }
}
