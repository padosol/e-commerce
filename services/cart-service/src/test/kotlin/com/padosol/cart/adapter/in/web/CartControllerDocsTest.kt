package com.padosol.cart.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.cart.application.port.`in`.CartItemView
import com.padosol.cart.application.port.`in`.CartUseCase
import com.padosol.cart.application.port.`in`.CartView
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
import java.util.UUID

@ExtendWith(RestDocumentationExtension::class)
class CartControllerDocsTest {

    private val cartUseCase = mockk<CartUseCase>()
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val productId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .subject(userId.toString())
        .build()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(CartController(cartUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .setCustomArgumentResolvers(CartJwtArgumentResolver(jwt))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    private fun sampleCart() = CartView(userId, listOf(CartItemView(productId, 2)))

    @Test
    fun `장바구니 담기 API 문서화`() {
        every { cartUseCase.addItem(userId, productId, 2) } returns sampleCart()

        mockMvc.perform(
            post("/api/carts/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"productId":"$productId","quantity":2}"""),
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "cart-add-item",
                    requestFields(
                        fieldWithPath("productId").description("상품 ID"),
                        fieldWithPath("quantity").description("수량(1 이상)"),
                    ),
                    responseFields(
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("items[]").description("장바구니 항목"),
                        fieldWithPath("items[].productId").description("상품 ID"),
                        fieldWithPath("items[].quantity").description("수량"),
                    ),
                ),
            )
    }

    @Test
    fun `내 장바구니 조회 API 문서화`() {
        every { cartUseCase.getCart(userId) } returns sampleCart()

        mockMvc.perform(get("/api/carts"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "cart-get",
                    responseFields(
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("items[]").description("장바구니 항목"),
                        fieldWithPath("items[].productId").description("상품 ID"),
                        fieldWithPath("items[].quantity").description("수량"),
                    ),
                ),
            )
    }
}

class CartJwtArgumentResolver(private val jwt: Jwt) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Jwt::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any = jwt
}
