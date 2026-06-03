# 사전 빌드: ./gradlew :services:api-gateway:bootJar
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
