package com.padosol.gateway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * 게이트웨이에서 JWT를 검증한다(Resource Server, reactive/WebFlux).
 * 인증/공개 엔드포인트는 통과시키고, 그 외 라우트는 유효한 JWT를 요구한다.
 * JWK는 auth-service의 JWKS 엔드포인트에서 가져온다(jwk-set-uri).
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/api/auth/**", "/actuator/**").permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
        return http.build()
    }
}
