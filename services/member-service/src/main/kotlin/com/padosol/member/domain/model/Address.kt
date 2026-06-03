package com.padosol.member.domain.model

/**
 * 배송지 값 객체.
 */
data class Address(
    val zipcode: String,
    val line1: String,
    val line2: String?,
)
