# One Dockerfile, three images. Pick the flavor with --target:
#   plain     no agent
#   newrelic  New Relic Java agent (-javaagent in the entrypoint)
#   otel      OpenTelemetry Java agent (attached through JAVA_TOOL_OPTIONS)

# Build the app once
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Shared runtime base
FROM eclipse-temurin:21-jre-alpine AS base
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
ENV SERVER_PORT=5100
EXPOSE 5100

FROM base AS plain
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM base AS newrelic
# Set NR_AGENT_VERSION to pin, e.g. 8.21.0
ARG NR_AGENT_VERSION=current
RUN wget -q -O /tmp/newrelic-java.zip \
    https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NR_AGENT_VERSION}/newrelic-java.zip \
    && unzip -q /tmp/newrelic-java.zip -d /app \
    && rm /tmp/newrelic-java.zip
ENV NEW_RELIC_APP_NAME=doodle-weather-java \
    NEW_RELIC_LOG_FILE_NAME=STDOUT \
    NEW_RELIC_DISTRIBUTED_TRACING_ENABLED=true
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "/app/app.jar"]

FROM base AS otel
RUN wget -q -O /app/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
