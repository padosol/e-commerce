plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen.plugin)
    implementation(libs.kotlin.noarg.plugin)
    implementation(libs.spring.boot.gradle.plugin)
    implementation(libs.spring.dependency.management.plugin)

    // 버전 카탈로그(libs) 접근자를 precompiled convention 플러그인에서 사용하기 위한 표준 우회.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
