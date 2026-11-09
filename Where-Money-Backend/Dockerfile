FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/Where-Money-Backend-1.0.jar app.jar
USER 10001:10001
EXPOSE 5050
ENTRYPOINT ["java","-Duser.timezone=Asia/Shanghai","-jar","app.jar"]
