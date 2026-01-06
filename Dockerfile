# ===== 1) Build stage: use Maven to build jar =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first for better cache
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies (cache)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw -DskipTests clean package

# ===== 2) Run stage: run the built jar =====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render will inject PORT, Spring reads it from server.port=${PORT:8080}
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
