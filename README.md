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

### 3. Run with the New Relic Java APM Agent
The image bundles the agent at `/app/newrelic/newrelic.jar` and starts it via `-javaagent`.
```bash
docker run -p 5100:5100 \
  -e NEW_RELIC_LICENSE_KEY=<your key> \
  -e NEW_RELIC_APP_NAME=doodle-weather-java \
  gitops-doodle-weather-java
```
Pin the agent version with `--build-arg NR_AGENT_VERSION=8.21.0` (default: `current`).

On Kubernetes the chart reads the key from secret `nr-license-key` (key `license-key`):
```bash
kubectl -n supreme-doodle create secret generic nr-license-key --from-literal=license-key=<your key>
```
