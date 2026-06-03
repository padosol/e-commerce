package com.padosol.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * 게이트웨이에서 JWT를 검증한다(Resource Server, reactive/WebFlux).
 * 브라우저(프론트)에서 직접 호출하므로 CORS를 허용하고, 인증/공개 엔드포인트는 통과시킨다.
 * JWK는 auth-service의 JWKS 엔드포인트에서 가져온다(jwk-set-uri).
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    @Value("\${cors.allowed-origins:http://localhost:3000}") private val allowedOrigins: String,
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeExchange {
                it.pathMatchers("/api/auth/**", "/actuator/**").permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = this@SecurityConfig.allowedOrigins.split(",").map { it.trim() }
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
