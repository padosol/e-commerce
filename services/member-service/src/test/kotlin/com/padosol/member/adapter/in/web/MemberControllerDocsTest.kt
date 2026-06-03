package com.padosol.member.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.member.application.port.`in`.AddAddressUseCase
import com.padosol.member.application.port.`in`.GetMyProfile
import com.padosol.member.application.port.`in`.MemberProfile
import com.padosol.member.domain.model.Address
import com.padosol.member.domain.model.MemberGrade
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

/**
 * 컨트롤러 계약 문서화(Spring REST Docs). @AuthenticationPrincipal Jwt 는 standalone 환경에서
 * 고정 Jwt 를 주입하는 커스텀 ArgumentResolver 로 대체한다.
 */
@ExtendWith(RestDocumentationExtension::class)
class MemberControllerDocsTest {

    private val getMyProfile = mockk<GetMyProfile>()
    private val addAddressUseCase = mockk<AddAddressUseCase>(relaxed = true)
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .subject(userId.toString())
        .claim("email", "user@example.com")
        .build()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(MemberController(getMyProfile, addAddressUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .setCustomArgumentResolvers(FixedJwtArgumentResolver(jwt))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    @Test
    fun `내 프로필 조회 API 문서화`() {
        every { getMyProfile.byUserId(userId) } returns MemberProfile(
            userId = userId,
            email = "user@example.com",
            nickname = "user",
            grade = MemberGrade.BRONZE,
            addresses = listOf(Address("12345", "서울특별시 1로", "201호")),
        )

        mockMvc.perform(get("/api/members/me"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "member-me",
                    responseFields(
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("nickname").description("닉네임"),
                        fieldWithPath("grade").description("회원 등급"),
                        fieldWithPath("addresses[]").description("배송지 목록"),
                        fieldWithPath("addresses[].zipcode").description("우편번호"),
                        fieldWithPath("addresses[].line1").description("기본 주소"),
                        fieldWithPath("addresses[].line2").description("상세 주소").optional(),
                    ),
                ),
            )
    }

    @Test
    fun `배송지 추가 API 문서화`() {
        mockMvc.perform(
            post("/api/members/me/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"zipcode":"12345","line1":"서울특별시 1로","line2":"201호"}"""),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "member-add-address",
                    requestFields(
                        fieldWithPath("zipcode").description("우편번호"),
                        fieldWithPath("line1").description("기본 주소"),
                        fieldWithPath("line2").description("상세 주소").optional(),
                    ),
                ),
            )
    }
}

class FixedJwtArgumentResolver(private val jwt: Jwt) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Jwt::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any = jwt
}
