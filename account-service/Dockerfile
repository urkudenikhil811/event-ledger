# ---- Stage 1: build the jar ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# copy the Maven wrapper and project definition first (for layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# now copy the source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: lean runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy ONLY the finished jar from the builder stage
COPY --from=builder /app/target/account-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]