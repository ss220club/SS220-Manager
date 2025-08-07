FROM maven:3.9.9-eclipse-temurin-24-alpine AS build
WORKDIR /build

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-alpine AS central-api
WORKDIR /app

COPY --from=build /build/central-api/target/club.ss220.central-api.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:24-alpine AS manager
WORKDIR /app

COPY --from=build /build/manager/target/club.ss220.manager.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
