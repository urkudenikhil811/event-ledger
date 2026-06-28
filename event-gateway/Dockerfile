# ---- Stage 1: build the jar ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: lean runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/event-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]