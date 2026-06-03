import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    "implementation"(libs.kotlin.reflect)
    "testImplementation"(libs.kotest.runner.junit5)
    "testImplementation"(libs.kotest.assertions.core)
    "testImplementation"(libs.mockk)
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        // 통합 테스트(@Tag("integration"))는 기본 빌드에서 제외 — 실 인프라가 필요하므로 integrationTest 태스크로 실행
        excludeTags("integration")
    }
}
