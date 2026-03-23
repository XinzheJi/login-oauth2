ARG RUNTIME_IMAGE=eclipse-temurin:17-jre

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 如需使用内网 Maven 私服，可根据需要调整 maven-settings.xml 并在构建时挂载
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM ${RUNTIME_IMAGE}
WORKDIR /app

COPY --from=builder /app/target/system-b-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
