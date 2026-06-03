package com.padosol.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 라우트는 k8s Service DNS 이름을 대상으로 한다(별도 디스커버리 클라이언트 없음).
 * 각 URI는 ConfigMap(환경변수 ROUTES_*)으로 override 가능하며, 로컬에선 기본값을 사용한다.
 *
 * NOTE: 대상 서비스(auth/member/product)는 Phase 1~2에서 추가된다. Phase 0에서는
 *       게이트웨이 라우팅 골격과 헬스 엔드포인트 검증이 목적이다.
 */
@Configuration
class GatewayRouteConfig {

    @Bean
    fun routeLocator(
        builder: RouteLocatorBuilder,
        @Value("\${routes.auth:http://auth-service:8080}") authUri: String,
        @Value("\${routes.member:http://member-service:8080}") memberUri: String,
        @Value("\${routes.product:http://product-service:8080}") productUri: String,
        @Value("\${routes.cart:http://cart-service:8080}") cartUri: String,
        @Value("\${routes.order:http://order-service:8080}") orderUri: String,
    ): RouteLocator = builder.routes()
        .route("auth-service") { r -> r.path("/api/auth/**").uri(authUri) }
        .route("member-service") { r -> r.path("/api/members/**").uri(memberUri) }
        .route("product-service") { r -> r.path("/api/products/**").uri(productUri) }
        .route("cart-service") { r -> r.path("/api/carts/**").uri(cartUri) }
        .route("order-service") { r -> r.path("/api/orders/**").uri(orderUri) }
        .build()
}
