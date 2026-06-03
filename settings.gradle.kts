pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "ecommerce"

include(
    ":services:api-gateway",
    ":services:auth-service",
    ":services:member-service",
    ":services:product-service",
    ":services:cart-service",
    ":services:inventory-service",
    ":libs:common-web",
    ":libs:common-security",
    ":libs:common-event",
)
