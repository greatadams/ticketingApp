#Stage 1: Build the java application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -pl events-service -am clean package -DskipTests

#Stage 2" Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/events-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]