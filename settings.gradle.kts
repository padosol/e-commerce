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
    ":libs:common-web",
    ":libs:common-security",
)
