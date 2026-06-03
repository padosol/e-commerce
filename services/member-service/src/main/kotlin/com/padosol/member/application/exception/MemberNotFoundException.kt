package com.padosol.member.application.exception

import java.util.UUID

class MemberNotFoundException(userId: UUID) :
    RuntimeException("Member not found for userId=$userId")
