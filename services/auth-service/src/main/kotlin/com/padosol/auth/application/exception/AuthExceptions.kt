package com.padosol.auth.application.exception

/**
 * 이미 가입된 이메일로 회원가입을 시도한 경우. 409로 매핑된다.
 */
class DuplicateEmailException(email: String) :
    RuntimeException("이미 사용 중인 이메일입니다: $email")

/**
 * 이메일/비밀번호가 일치하지 않는 경우. 401로 매핑된다.
 */
class InvalidCredentialsException :
    RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.")
