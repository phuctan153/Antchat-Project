# Bước 1: Build ứng dụng (Sử dụng Maven và Java 21)
FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Tận dụng cache của Docker để tải dependencies trước
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code và tiến hành build file .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng (Sử dụng JRE 21 để tối ưu dung lượng)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar đã build từ bước trên sang
COPY --from=builder /app/target/*.jar app.jar

# Mở port 8080 cho Spring Boot
EXPOSE 8080

# Chạy ứng dụng AntChat
ENTRYPOINT ["java", "-jar", "app.jar"]