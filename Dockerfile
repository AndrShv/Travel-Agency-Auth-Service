FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/travel-agency-modified-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
