package com.padosol.common.web

import java.time.Instant

/**
 * 모든 서비스가 공유하는 표준 에러 응답 형태 (기술 횡단 관심사).
 * 도메인 코드는 절대 포함하지 않는다.
 */
data class ApiError(
    val code: String,
    val message: String,
    val status: Int,
    val path: String? = null,
    val timestamp: Instant = Instant.now(),
)
