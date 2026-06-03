package com.padosol.auth.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder

/**
 * 시작 시 인메모리 RSA 키쌍을 생성한다(이 단계에선 키 영속화/회전 없음).
 * 공개 JWK는 JwksController가 /oauth2/jwks로 노출한다.
 */
@Configuration
class JwtConfig {

    @Bean
    fun rsaKey(): RSAKey =
        RSAKeyGenerator(2048)
            .keyID("auth-key")
            .generate()

    @Bean
    fun jwkSource(rsaKey: RSAKey): JWKSource<SecurityContext> =
        ImmutableJWKSet(JWKSet(rsaKey))

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder =
        NimbusJwtEncoder(jwkSource)
}
