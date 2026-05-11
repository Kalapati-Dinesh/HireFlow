FROM eclipse-temurin:21

WORKDIR /app

COPY target/HireFlow-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
