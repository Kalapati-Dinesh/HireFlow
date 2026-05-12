# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/HireFlow-0.0.1-SNAPSHOT.jar app.jar

# Railway injects PORT at runtime — don't hardcode it
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
