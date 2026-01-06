# 1. Use the official Java 17 runtime image
FROM eclipse-temurin:17-jdk-jammy

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy Maven configuration and wrapper first
#    (to leverage Docker layer caching)
COPY pom.xml mvnw ./
COPY .mvn .mvn

# 4. Download dependencies
#    (this layer will be cached if pom.xml does not change)
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# 5. Copy application source code
COPY src src


# 6. Build the application JAR
#    (skip tests to speed up the build)
RUN ./mvnw package -DskipTests

# 7. Run the Spring Boot application
CMD ["java", "-jar", "target/*.jar"]
