package com.padosol.auth.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.padosol.auth.application.port.`in`.LoginUseCase
import com.padosol.auth.application.port.`in`.SignUpUseCase
import com.padosol.auth.application.port.`in`.Tokens
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.util.UUID

/**
 * 컨트롤러 계약 문서화(Spring REST Docs). 유스케이스는 MockK 로 대체하고
 * standalone MockMvc 로 컨텍스트 없이 빠르게 문서 스니펫을 생성한다(build/generated-snippets).
 */
@ExtendWith(RestDocumentationExtension::class)
class AuthControllerDocsTest {

    private val signUpUseCase = mockk<SignUpUseCase>()
    private val loginUseCase = mockk<LoginUseCase>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.standaloneSetup(AuthController(signUpUseCase, loginUseCase))
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
            .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
    }

    @Test
    fun `회원가입 API 문서화`() {
        every { signUpUseCase.signUp(any()) } returns UUID.fromString("11111111-1111-1111-1111-111111111111")

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"user@example.com","password":"pw12345!"}"""),
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "auth-signup",
                    requestFields(
                        fieldWithPath("email").description("가입 이메일"),
                        fieldWithPath("password").description("비밀번호(평문)"),
                    ),
                    responseFields(
                        fieldWithPath("userId").description("생성된 사용자 ID(UUID)"),
                    ),
                ),
            )
    }

    @Test
    fun `로그인 API 문서화`() {
        every { loginUseCase.login(any()) } returns Tokens("access-token", "refresh-token", 900)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"user@example.com","password":"pw12345!"}"""),
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "auth-login",
                    requestFields(
                        fieldWithPath("email").description("이메일"),
                        fieldWithPath("password").description("비밀번호(평문)"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("액세스 토큰(JWT)"),
                        fieldWithPath("refreshToken").description("리프레시 토큰(JWT)"),
                        fieldWithPath("expiresInSeconds").description("액세스 토큰 만료(초)"),
                    ),
                ),
            )
    }
}
