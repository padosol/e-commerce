plugins {
    id("ecommerce.spring-library")
}

dependencies {
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
