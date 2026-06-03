package com.padosol.auth.adapter.`in`.web

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 리소스 서버가 액세스 토큰 서명을 검증할 수 있도록 공개 JWK Set을 노출한다.
 */
@RestController
class JwksController(
    private val rsaKey: RSAKey,
) {

    @GetMapping("/oauth2/jwks")
    fun jwks(): Map<String, Any> {
        val rsaPublicJwk = rsaKey.toPublicJWK()
        return JWKSet(rsaPublicJwk).toJSONObject()
    }
}
