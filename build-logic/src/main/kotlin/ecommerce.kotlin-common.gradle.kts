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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
