# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /src/target/audit-service-*.jar app.jar
USER app
EXPOSE 4000
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
# Probes for orchestrators: /api/actuator/health/liveness and /api/actuator/health/readiness
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
