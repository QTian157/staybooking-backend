# ====== build stage ======
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# 先拷贝 Maven 配置
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 预下载依赖（利用 Docker cache）
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# 再拷贝源码
COPY src src

# 构建 jar
RUN ./mvnw -B package -DskipTests

# ====== runtime stage ======
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 只拷贝最终 jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
