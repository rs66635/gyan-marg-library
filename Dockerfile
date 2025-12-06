# Multi-stage Dockerfile for GyanMarg Library
# Build with Maven (JDK 21), run with a lightweight JRE

FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy Maven wrapper and pom first for caching
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn

# Copy source
COPY src ./src

# Build the application (skip tests for speed)
RUN mvn -B -DskipTests package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from builder
COPY --from=builder /workspace/target/gyan-marg-library-0.0.1-SNAPSHOT.jar ./app.jar

ENV JAVA_TOOL_OPTIONS=-Xmx512m
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]