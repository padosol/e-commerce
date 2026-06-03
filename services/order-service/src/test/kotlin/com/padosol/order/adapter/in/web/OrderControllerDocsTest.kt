package com.padosol.order.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.order.application.port.`in`.GetOrderUseCase
import com.padosol.order.application.port.`in`.OrderLineView
import com.padosol.order.application.port.`in`.OrderView
import com.padosol.order.application.port.`in`.PlaceOrderUseCase
import com.padosol.order.domain.model.OrderStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(RestDocumentationExtension::class)
class OrderControllerDocsTest {

    private val placeOrderUseCase = mockk<PlaceOrderUseCase>()
    private val getOrderUseCase = mockk<GetOrderUseCase>()
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val orderId = UUID.fromString("66666666-6666-6666-6666-666666666666")
    private val productId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val jwt = Jwt.withTokenValue("test-token").header("alg", "none").subject(userId.toString()).build()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(OrderController(placeOrderUseCase, getOrderUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .setCustomArgumentResolvers(OrderJwtArgumentResolver(jwt))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun confirmedView() = OrderView(
        id = orderId,
        userId = userId,
        status = OrderStatus.CONFIRMED,
        totalAmount = BigDecimal("25000"),
        lines = listOf(OrderLineView(productId, 2, BigDecimal("10000"))),
    )

    @Test
    fun `주문(체크아웃) API 문서화`() {
        every { placeOrderUseCase.place(any()) } returns confirmedView()

        mockMvc.perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"lines":[{"productId":"$productId","quantity":2,"unitPrice":10000}],
                     "shippingAddress":{"zipcode":"12345","line1":"서울 1로","line2":"201호"}}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "order-place",
                    requestFields(
                        fieldWithPath("lines[].productId").description("상품 ID"),
                        fieldWithPath("lines[].quantity").description("수량"),
                        fieldWithPath("lines[].unitPrice").description("단가"),
                        fieldWithPath("shippingAddress.zipcode").description("우편번호"),
                        fieldWithPath("shippingAddress.line1").description("기본 주소"),
                        fieldWithPath("shippingAddress.line2").description("상세 주소").optional(),
                    ),
                    responseFields(
                        fieldWithPath("id").description("주문 ID"),
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("status").description("주문 상태"),
                        fieldWithPath("totalAmount").description("총 결제 금액"),
                        fieldWithPath("lines[].productId").description("상품 ID"),
                        fieldWithPath("lines[].quantity").description("수량"),
                        fieldWithPath("lines[].unitPrice").description("단가"),
                    ),
                ),
            )
    }

    @Test
    fun `주문 조회 API 문서화`() {
        every { getOrderUseCase.get(orderId) } returns confirmedView()

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "order-get",
                    responseFields(
                        fieldWithPath("id").description("주문 ID"),
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("status").description("주문 상태"),
                        fieldWithPath("totalAmount").description("총 결제 금액"),
                        fieldWithPath("lines[].productId").description("상품 ID"),
                        fieldWithPath("lines[].quantity").description("수량"),
                        fieldWithPath("lines[].unitPrice").description("단가"),
                    ),
                ),
            )
    }
}

class OrderJwtArgumentResolver(private val jwt: Jwt) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Jwt::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any = jwt
}
