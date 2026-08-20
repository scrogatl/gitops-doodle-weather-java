# gitops-doodle-weather-java

Java 21 / Spring Boot 3 implementation of the Supreme Doodle Weather service. 100% API compatible with `gitops-doodle-weather` (.NET).

## Ports & Endpoints

- **Port**: `5100` (configurable via `SERVER_PORT` or `PORT`)
- **Endpoints**:
  - `GET /weatherforecast` — Returns randomized weather forecast array matching the .NET schema.
  - `GET /health` — Health check endpoint.
  - `GET /actuator/prometheus` — Prometheus metrics scraping endpoint.

## Containerized Build & Run (No local Java required)

### 1. Build Docker Image
```bash
docker build -t gitops-doodle-weather-java .
```

### 2. Run Locally
```bash
docker run -p 5100:5100 -e SHARD=local gitops-doodle-weather-java
```

### 3. Run with OpenTelemetry Auto-Instrumentation
```bash
docker run -p 5100:5100 \
  -e JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar" \
  -e OTEL_SERVICE_NAME="weather-java-otel" \
  -e OTEL_EXPORTER_OTLP_ENDPOINT="http://nrdot-collector:4317" \
  gitops-doodle-weather-java
```
