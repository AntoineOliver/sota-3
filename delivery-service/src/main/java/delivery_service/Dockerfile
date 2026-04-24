# Utiliser un JDK léger
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copier le JAR du service
COPY build/libs/delivery_service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
