package com.padosol.common.security

/**
 * 게이트웨이가 JWT 검증 후 하위 서비스로 전파하는 사용자 컨텍스트 헤더 키.
 */
object AuthHeaders {
    const val USER_ID = "X-User-Id"
    const val USER_ROLES = "X-User-Roles"
}
