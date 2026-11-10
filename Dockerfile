# Stage 1: build the jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Stage 2: small runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/online-exam-portal-1.0.0.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
# So saved times show in Indian time
ENV TZ=Asia/Kolkata
ENTRYPOINT ["java", "-jar", "app.jar"]
