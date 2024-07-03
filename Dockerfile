FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/spring-boot-resilience4j-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]