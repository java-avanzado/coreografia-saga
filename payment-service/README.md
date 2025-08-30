# payment-service

Servicio de pagos. Consume `OrderCreated` y publica `PaymentAuthorized` o
`PaymentFailed`. También escucha `CompensatePayment` y publica
`PaymentReverted`.

## Ejecutar

1. Levanta el broker:

```bash
docker compose up -d
```

2. Ejecuta el servicio:

```bash
mvn -q -pl payment-service -am spring-boot:run
```

## Configuración

- Variables de entorno:
    - `PAYMENT_FAIL_RATIO` (0.0 - 1.0). Por defecto `0.2`.

- Tópicos:
    - Entrada: `orders.created`, `payments.compensate`
    - Salida: `payments.authorized`, `payments.failed`, `payments.reverted`

## Notas

- Este servicio no expone endpoints HTTP; trabaja únicamente por eventos.
