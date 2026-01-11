# ranking-service

Microservicio para calcular rankings de colecciones de películas por usuario basándose en rareza + atributos.

## Variables de entorno / configuración

Estas propiedades siguen el `application.properties` del repositorio:

- `spring.kafka.bootstrap-servers`: broker de Kafka.
- `ranking.user-item-changed-topic`: tópico de cambios en items de usuario.
- `ranking.user-status-changed-topic`: tópico de cambios de estado de usuario.
- `ranking.item-rarity-updated-topic`: tópico interno para rareza recalculada.
- `ranking.elasticsearch-uri`: URL del cluster de Elasticsearch.
- `ranking.top-k`: número de items por usuario a sumar en el score.
- `ranking.m`: parámetro de suavizado para rareza.
- `ranking.batch-size`: tamaño de lote para iteraciones en Elasticsearch.
- `ranking.weights.*`: pesos configurables para edition, condition y completeness.

## Levantar dependencias

Usa la infraestructura del repo:

```bash
docker-compose up -d
```

## Envelopes de eventos (según plantilla)

Los eventos siguen el envelope de dominio (`request_id`, `event_type`) y añaden `event_id`.

### UserItemChanged

```json
{
  "request_id": "0d7cce6d-4b55-4e33-9ee6-f3232f02f4b6",
  "event_type": "UserItemChanged",
  "event_id": "0d7cce6d-4b55-4e33-9ee6-f3232f02f4b6",
  "user_id": "user-123",
  "item_id": "item-456",
  "action": "UPSERT",
  "edition": "LIMITED",
  "condition": "SEALED",
  "completeness": "FULL",
  "occurred_on": "2025-01-01T12:00:00"
}
```

### UserStatusChanged

```json
{
  "request_id": "9d5a4f9c-1ad9-42de-9af3-2c1c9f98a1f5",
  "event_type": "UserStatusChanged",
  "event_id": "9d5a4f9c-1ad9-42de-9af3-2c1c9f98a1f5",
  "user_id": "user-123",
  "status": "ACTIVE",
  "occurred_on": "2025-01-01T12:05:00"
}
```

### ItemRarityUpdated (publicado por ranking-service)

```json
{
  "request_id": "a6d1e9f2-9b2a-46a4-8f42-5b8f3b2d5fd8",
  "event_type": "ItemRarityUpdated",
  "event_id": "a6d1e9f2-9b2a-46a4-8f42-5b8f3b2d5fd8",
  "item_id": "item-456",
  "owners": 25,
  "active_users": 100,
  "rarity": 0.51,
  "occurred_on": "2025-01-01T12:10:00"
}
```

## Endpoints

### Health

```bash
curl -X GET http://localhost:8089/health
```

### User score

```bash
curl -X GET http://localhost:8089/users/user-123/score
```

### Leaderboard

```bash
curl -X GET "http://localhost:8089/leaderboard?limit=50"
```
