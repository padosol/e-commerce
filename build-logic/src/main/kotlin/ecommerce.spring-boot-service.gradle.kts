import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.accessors.dm.LibrariesForLibs

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
}
