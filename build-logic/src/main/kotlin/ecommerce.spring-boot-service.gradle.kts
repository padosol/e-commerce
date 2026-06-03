import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

plugins {
    id("ecommerce.kotlin-common")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
    kotlin("plugin.jpa") // @Entity 등에 no-arg 생성자 생성 (Hibernate 인스턴스화)
}

val libs = the<LibrariesForLibs>()

// 부트 플러그인이 Spring Boot BOM을 자동 import하므로 여기서는 Spring Cloud BOM만 추가.
configure<DependencyManagementExtension> {
    imports {
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}

dependencies {
    "implementation"(libs.jackson.module.kotlin)
    // 분산추적(전 서비스 공통): Micrometer Tracing + OpenTelemetry → Zipkin
    "implementation"("io.micrometer:micrometer-tracing-bridge-otel")
    "implementation"("io.opentelemetry:opentelemetry-exporter-zipkin")
}

// Testcontainers 기반 통합 테스트는 별도 태스크로 분리 (실 DB/Kafka 필요)
tasks.register<Test>("integrationTest") {
    description = "Testcontainers 기반 통합 테스트 (@Tag(\"integration\"))"
    group = "verification"
    val testSourceSet = the<SourceSetContainer>()["test"]
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform { includeTags("integration") }
    shouldRunAfter("test")
}
