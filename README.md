# Event Ledger

A distributed system composed of two microservices that process financial transaction events with idempotency, out-of-order tolerance, distributed tracing, observability, and resiliency.

---

## Architecture

**Event Gateway** receives transaction events from clients, enforces idempotency, stores event records in its own database, and forwards new events to the Account Service.

**Account Service** manages account state — applying transactions, computing balances, and serving account-level queries. It is only called by the Gateway, never exposed directly.

Each service has its own in-memory H2 database and shares no state with the other.

---

## Prerequisites

- Java 21 (Temurin recommended)
- Maven wrapper included — no separate Maven install needed

---

## Running the Services

Start each service in a separate terminal from the repo root.

**Account Service (port 8080):**
```bash
cd account-service
./mvnw spring-boot:run
```

**Event Gateway (port 8081):**
```bash
cd event-gateway
./mvnw spring-boot:run
```

Wait for `Started AccountServiceApplication` and `Started EventGatewayApplication` in each console before sending requests.

---

## Running the Tests

```bash
# Account Service
cd account-service
./mvnw test

# Event Gateway
cd event-gateway
./mvnw test
```

Tests cover idempotency, out-of-order balance computation, validation, 201/200 status distinction, 503 graceful degradation, and trace propagation.

---

## API Quick Reference

### Event Gateway (port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/events` | Submit a transaction event |
| GET | `/events/{id}` | Retrieve a single event by ID |
| GET | `/events?account={accountId}` | List events for an account, ordered by timestamp |
| GET | `/health` | Health check with DB status |

### Account Service (port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/accounts/{accountId}/transactions` | Apply a transaction |
| GET | `/accounts/{accountId}/balance` | Get current balance |
| GET | `/accounts/{accountId}` | Get account details and transactions |
| GET | `/health` | Health check with DB status |

### Example Request

```bash
curl -X POST localhost:8081/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "accountId": "acct-123",
    "type": "CREDIT",
    "amount": 150.00,
    "currency": "USD",
    "eventTimestamp": "2026-05-15T14:02:11Z",
    "metadata": { "source": "mainframe-batch" }
  }'
```

---

## Design Decisions

### No service discovery (Eureka)
The Account Service URL is configurable via `account-service.base-url` in the Gateway's `application.properties`. This points at `localhost:8080` locally and can be set to `http://account-service:8080` in Docker or a Kubernetes Service DNS name without any code change. Scalability comes from statelessness and idempotency, not from discovery — both services are horizontally scalable behind any load balancer.

### Balance computed, not stored
Balance is derived at query time as `sum(CREDITs) − sum(DEBITs)`. Because addition is commutative and associative, out-of-order event arrival is a non-problem — the result is always correct regardless of insertion order.

### Idempotency via unique constraint + find-or-return
The Gateway keys on `eventId`; the Account Service keys on `sourceEventId` (the same value). Both use a database-level `UNIQUE` constraint as a backstop against concurrent duplicates. The Gateway applies the transaction to the Account Service first, then saves the event locally — so a failure during the Account call leaves nothing stored, and a retry starts clean.

### Resiliency: timeout + bounded retry with backoff
The Gateway uses a 2-second connect/read timeout and retries the Account Service call up to 3 times with linear backoff (200ms, 400ms). Retrying a POST is safe here because the Account Service is idempotent on `sourceEventId` — a retried apply can never double-count. After 3 failures, the Gateway returns `503 Service Unavailable`. Read-only Gateway endpoints (`GET /events/{id}`, `GET /events?account=...`) depend only on the Gateway's own database and remain available when the Account Service is down.

### Distributed tracing via MDC + X-Trace-Id header
A servlet filter on each service reads the `X-Trace-Id` request header (or generates a UUID if absent) and stores it in the SLF4J MDC. Every log line emitted during that request automatically carries the `traceId` field. The Gateway copies the MDC value onto the outgoing `X-Trace-Id` header when calling the Account Service, so a single client request produces a traceable path across both services in the structured logs.

### Structured logging (ECS format)
Both services emit JSON logs in Elastic Common Schema format via Spring Boot's built-in structured logging (`logging.structured.format.console=ecs`). No external dependencies required.

### Custom metric
The Gateway exposes an `events.received` counter via Micrometer, accessible at `GET /metrics/events.received`. This counts every POST attempt (including duplicates).

---

## Observability

Both services expose `/health` (with live database connectivity check) and `/metrics` via Spring Boot Actuator. JSON structured logs include `@timestamp`, `log.level`, `service.name`, `traceId`, and `message` on every line.

---

## Running with Docker Compose

From the repo root:

    docker compose up -d

This starts both services. The Gateway reaches the Account Service by its
container name (`http://account-service:8080`), set via the
`ACCOUNT-SERVICE_BASE-URL` environment variable in `docker-compose.yml`.

Stop everything with:

    docker compose down

## Bonus Items Considered

- **OpenTelemetry / Jaeger**: manual `X-Trace-Id` propagation used instead; OTel Collector integration would be a natural next step.
- **Prometheus**: Micrometer metrics are Prometheus-compatible; a `/actuator/prometheus` endpoint can be enabled by adding `micrometer-registry-prometheus` to the Gateway's `pom.xml`.
- **Async fallback queue**: when the Account Service is unavailable, events are rejected with 503 rather than queued; a local queue with replay-on-recovery would be the production extension.