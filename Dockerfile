# -------- Стадия 1 билд --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline


COPY src ./src
RUN mvn -q -DskipTests clean package

# -------- Стадия 2 runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app


COPY --from=build /app/target/*.jar app.jar


ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Запуск
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]