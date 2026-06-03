package com.padosol.member.adapter.`in`.web

import com.padosol.member.application.port.`in`.AddAddressUseCase
import com.padosol.member.application.port.`in`.GetMyProfile
import com.padosol.member.application.port.`in`.MemberProfile
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val getMyProfile: GetMyProfile,
    private val addAddressUseCase: AddAddressUseCase,
) {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal jwt: Jwt): MemberProfile =
        getMyProfile.byUserId(UUID.fromString(jwt.subject))

    @PostMapping("/me/addresses")
    fun addAddress(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: AddAddressRequest,
    ): ResponseEntity<Void> {
        addAddressUseCase.addAddress(UUID.fromString(jwt.subject), request.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
