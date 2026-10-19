FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY common/pom.xml ./common/pom.xml
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY sa-engine-service/pom.xml ./sa-engine-service/pom.xml
COPY common/src ./common/src
COPY inventory-service/src ./inventory-service/src
COPY sa-engine-service/src ./sa-engine-service/src
RUN mvn --batch-mode --projects sa-engine-service --also-make --define skipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/sa-engine-service/target/sa-engine-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
