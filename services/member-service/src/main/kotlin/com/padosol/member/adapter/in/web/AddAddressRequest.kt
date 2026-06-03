package com.padosol.member.adapter.`in`.web

import com.padosol.member.domain.model.Address
import jakarta.validation.constraints.NotBlank

data class AddAddressRequest(
    @field:NotBlank
    val zipcode: String,

    @field:NotBlank
    val line1: String,

    val line2: String?,
) {
    fun toDomain(): Address = Address(zipcode = zipcode, line1 = line1, line2 = line2)
}
