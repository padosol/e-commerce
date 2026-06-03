package com.padosol.payment.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.payment.application.port.`in`.PaymentUseCase
import com.padosol.payment.application.port.`in`.PaymentView
import com.padosol.payment.domain.model.PaymentStatus
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
class PaymentControllerDocsTest {

    private val paymentUseCase = mockk<PaymentUseCase>()
    private val paymentId = UUID.fromString("33333333-3333-3333-3333-333333333333")
    private val orderId = UUID.fromString("44444444-4444-4444-4444-444444444444")
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(PaymentController(paymentUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun approvedView() =
        PaymentView(paymentId, orderId, BigDecimal("10000"), PaymentStatus.APPROVED, "tx-1")

    @Test
    fun `결제 승인 API 문서화`() {
        every { paymentUseCase.approve(any()) } returns approvedView()

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"$orderId","amount":10000,"idempotencyKey":"key-1"}"""),
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "payment-approve",
                    requestFields(
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("amount").description("결제 금액"),
                        fieldWithPath("idempotencyKey").description("멱등 키(중복 승인 방지)"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("결제 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("amount").description("결제 금액"),
                        fieldWithPath("status").description("결제 상태"),
                        fieldWithPath("transactionId").description("PG 거래번호").optional(),
                    ),
                ),
            )
    }

    @Test
    fun `결제 취소 API 문서화`() {
        every { paymentUseCase.cancel(paymentId) } returns
            PaymentView(paymentId, orderId, BigDecimal("10000"), PaymentStatus.CANCELLED, "tx-1")

        mockMvc.perform(post("/api/payments/{id}/cancellations", paymentId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "payment-cancel",
                    responseFields(
                        fieldWithPath("id").description("결제 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("amount").description("결제 금액"),
                        fieldWithPath("status").description("결제 상태"),
                        fieldWithPath("transactionId").description("PG 거래번호").optional(),
                    ),
                ),
            )
    }

    @Test
    fun `결제 조회 API 문서화`() {
        every { paymentUseCase.get(paymentId) } returns approvedView()

        mockMvc.perform(get("/api/payments/{id}", paymentId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "payment-get",
                    responseFields(
                        fieldWithPath("id").description("결제 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("amount").description("결제 금액"),
                        fieldWithPath("status").description("결제 상태"),
                        fieldWithPath("transactionId").description("PG 거래번호").optional(),
                    ),
                ),
            )
    }
}
