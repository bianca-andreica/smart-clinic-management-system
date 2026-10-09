
# Stage 1: Build the Spring Boot application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy the Maven project files
COPY app/pom.xml ./pom.xml
COPY app/src ./src

# Compile and package the application
RUN mvn -B -DskipTests package

# Stage 2: Run the application using a lightweight Java runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the generated executable JAR from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Spring Boot default application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
