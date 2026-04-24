FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/dronefleet_service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
