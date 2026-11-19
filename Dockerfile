FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /app
COPY ${SERVICE}/pom.xml .
RUN mvn -q dependency:go-offline
COPY ${SERVICE}/src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]