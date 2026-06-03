package com.padosol.shipping.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.shipping.application.port.`in`.ShipmentView
import com.padosol.shipping.application.port.`in`.ShippingUseCase
import com.padosol.shipping.domain.model.ShipmentStatus
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
class ShipmentControllerDocsTest {

    private val shippingUseCase = mockk<ShippingUseCase>()
    private val shipmentId = UUID.fromString("55555555-5555-5555-5555-555555555555")
    private val orderId = UUID.fromString("44444444-4444-4444-4444-444444444444")
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(ShipmentController(shippingUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun view(status: ShipmentStatus, tracking: String?) =
        ShipmentView(shipmentId, orderId, "12345", "서울특별시 1로", "201호", status, tracking)

    @Test
    fun `배송 생성 API 문서화`() {
        every { shippingUseCase.create(any()) } returns view(ShipmentStatus.CREATED, null)

        mockMvc.perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"$orderId","zipcode":"12345","line1":"서울특별시 1로","line2":"201호"}"""),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "shipment-create",
                    requestFields(
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("zipcode").description("우편번호"),
                        fieldWithPath("line1").description("기본 주소"),
                        fieldWithPath("line2").description("상세 주소").optional(),
                    ),
                    responseFields(
                        fieldWithPath("id").description("배송 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("zipcode").description("우편번호"),
                        fieldWithPath("line1").description("기본 주소"),
                        fieldWithPath("line2").description("상세 주소").optional(),
                        fieldWithPath("status").description("배송 상태"),
                        fieldWithPath("trackingNumber").description("운송장 번호").optional(),
                    ),
                ),
            )
    }

    @Test
    fun `배송 출고 API 문서화`() {
        every { shippingUseCase.ship(shipmentId, "TRACK-1") } returns view(ShipmentStatus.IN_TRANSIT, "TRACK-1")

        mockMvc.perform(
            post("/api/shipments/{id}/dispatch", shipmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"trackingNumber":"TRACK-1"}"""),
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "shipment-dispatch",
                    requestFields(
                        fieldWithPath("trackingNumber").description("운송장 번호"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("배송 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("zipcode").description("우편번호"),
                        fieldWithPath("line1").description("기본 주소"),
                        fieldWithPath("line2").description("상세 주소").optional(),
                        fieldWithPath("status").description("배송 상태"),
                        fieldWithPath("trackingNumber").description("운송장 번호").optional(),
                    ),
                ),
            )
    }

    @Test
    fun `배송 추적(조회) API 문서화`() {
        every { shippingUseCase.get(shipmentId) } returns view(ShipmentStatus.IN_TRANSIT, "TRACK-1")

        mockMvc.perform(get("/api/shipments/{id}", shipmentId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "shipment-get",
                    responseFields(
                        fieldWithPath("id").description("배송 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("zipcode").description("우편번호"),
                        fieldWithPath("line1").description("기본 주소"),
                        fieldWithPath("line2").description("상세 주소").optional(),
                        fieldWithPath("status").description("배송 상태"),
                        fieldWithPath("trackingNumber").description("운송장 번호").optional(),
                    ),
                ),
            )
    }
}
