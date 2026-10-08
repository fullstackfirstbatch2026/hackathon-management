FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:24-jre
WORKDIR /app
RUN useradd --system --create-home spring
COPY --from=build /workspace/target/management-0.0.1-SNAPSHOT.jar /app/app.jar
USER spring
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
