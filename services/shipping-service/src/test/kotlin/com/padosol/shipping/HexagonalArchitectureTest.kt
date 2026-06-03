package com.padosol.shipping

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@AnalyzeClasses(packages = ["com.padosol.shipping"], importOptions = [ImportOption.DoNotIncludeTests::class])
class HexagonalArchitectureTest {

    @ArchTest
    fun `domain 은 다른 레이어에 의존하지 않는다`(classes: JavaClasses) {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..adapter..")
            .check(classes)
    }

    @ArchTest
    fun `domain 은 스프링·JPA 프레임워크에 의존하지 않는다`(classes: JavaClasses) {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
            .check(classes)
    }

    @ArchTest
    fun `application 은 adapter 에 의존하지 않는다`(classes: JavaClasses) {
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .check(classes)
    }
}
