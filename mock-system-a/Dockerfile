FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 如果要用私服，这里你后面可以在 settings.xml 里引用 MAVEN_NEXUS_REPO
ENV MAVEN_NEXUS_REPO=http://192.168.106.23:18082/repository/maven-public/

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

