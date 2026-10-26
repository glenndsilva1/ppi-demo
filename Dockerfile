FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17.0.18_8-jre
COPY --from=build /app/target/ppi-demo.jar /app.jar
CMD ["java", "-XX:MaxRAMPercentage=75", "-Djava.net.preferIPv4Stack=true", "-jar", "/app.jar"]
