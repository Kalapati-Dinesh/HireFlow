FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd -m -u 1000 user
USER user
WORKDIR /app
COPY --chown=user --from=build /app/target/*.jar app.jar
EXPOSE 7860
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=7860"]
