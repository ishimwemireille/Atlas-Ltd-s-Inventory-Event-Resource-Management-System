# ── Stage 1: Build the JAR with Maven ─────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first so dependency layer is cached separately
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (skip tests — tests run in CI, not in Docker build)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Run the JAR on a lightweight JRE ──────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
