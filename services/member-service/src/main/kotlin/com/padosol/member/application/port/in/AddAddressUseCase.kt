package com.padosol.member.application.port.`in`

import com.padosol.member.domain.model.Address
import java.util.UUID

interface AddAddressUseCase {
    fun addAddress(userId: UUID, address: Address)
}
