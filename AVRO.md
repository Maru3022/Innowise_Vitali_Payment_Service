# Avro + Schema Registry for Payment Service

## Infrastructure

Run the stack:

```bash
docker compose up -d zookeeper kafka schema-registry mongodb payment-service
```

## Schema Registry checks

```bash
curl http://localhost:8081/subjects
curl http://localhost:8081/subjects/com.example.events-value/versions
curl http://localhost:8081/config/com.example.events-value
```

## Compatibility

```bash
curl -X PUT http://localhost:8081/config/com.example.events-value \
  -H 'Content-Type: application/json' \
  -d '{"compatibility":"BACKWARD"}'
```
