# transport-service

Servicio de transporte. Consume `PaymentAuthorized` y publica `ShipmentReserved`
o `ShipmentFailed`.

## Ejecutar

1. Levanta el broker:

```bash
docker compose up -d
```

2. Ejecuta el servicio:

```bash
mvn -q -pl transport-service -am spring-boot:run
```

## Configuración

- Variables de entorno:
    - `SHIPMENT_FAIL_RATIO` (0.0 - 1.0). Por defecto `0.2`.

- Tópicos:
    - Entrada: `payments.authorized`
    - Salida: `shipments.reserved`, `shipments.failed`

## Notas

- Este servicio no expone endpoints HTTP; trabaja únicamente por eventos.
