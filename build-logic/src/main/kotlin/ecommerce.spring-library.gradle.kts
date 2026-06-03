import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("ecommerce.kotlin-common")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

val libs = the<LibrariesForLibs>()

// 공유 라이브러리: Spring BOM으로 버전만 정렬, 실행 가능한 부트 jar는 만들지 않는다.
configure<DependencyManagementExtension> {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}
