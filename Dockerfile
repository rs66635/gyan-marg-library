# Production Dockerfile - copies the pre-built JAR into a small JRE image
# Build the jar locally first: mvn -DskipTests package

FROM eclipse-temurin:21-jre as runtime
WORKDIR /app

# Copy the repackaged Spring Boot jar from the Maven target directory
COPY target/gyan-marg-library-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS=-Xmx512m
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]