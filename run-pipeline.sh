#!/bin/bash
set -e

ACCOUNT_CONTAINER="event-ledger-account-service-1"

echo "=== [1/6] Starting SIT ==="
docker compose up -d

echo "=== [2/6] Waiting for SIT ==="
until curl -sf localhost:8080/health > /dev/null && curl -sf localhost:8081/health > /dev/null; do
  echo "  waiting..."
  sleep 2
done
echo "  SIT is up"

echo "=== [3/6] Smoke tests against SIT ==="
(cd admf-tests && ./mvnw -q test -Dkarate.env=sit -Dkarate.options="--tags @smoke")

echo "=== [4/6] Regression tests against SIT ==="
(cd admf-tests && ./mvnw -q test -Dkarate.env=sit -Dkarate.options="--tags @regression")

echo "=== [5/6] Starting UAT ==="
docker compose -f docker-compose.uat.yml -p event-ledger-uat up -d
until curl -sf localhost:9080/health > /dev/null && curl -sf localhost:9081/health > /dev/null; do
  echo "  waiting..."
  sleep 2
done
echo "  UAT is up"

echo "=== [6/6] Smoke tests against UAT ==="
(cd admf-tests && ./mvnw -q test -Dkarate.env=uat -Dkarate.options="--tags @smoke")

echo ""
echo "=== PIPELINE PASSED ==="