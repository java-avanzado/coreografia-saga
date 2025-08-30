# order-coordinator

Servicio coordinador de la SAGA por coreografía. Expone un endpoint HTTP para
crear órdenes y publica eventos `OrderCreated`. Además, escucha eventos de pago
y de envío para marcar la orden como completada o lanzar compensaciones.

## Ejecutar

1. Asegúrate de tener el broker levantado:

```bash
docker compose up -d
```

2. Ejecuta el servicio:

```bash
mvn -q -pl order-coordinator -am spring-boot:run
```

3. Crear una orden (ejemplo):

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"user-123",
    "amount": 49.99,
    "address":"Calle Falsa 123"
  }'
```

La respuesta devuelve `orderId`. El servicio publicará `OrderCreated` en
`orders.created`.

## Configuración

- Tópicos usados:
    - `orders.created` (salida): pedido creado.
    - `payments.authorized` (entrada): pago autorizado.
    - `payments.failed` (entrada): pago fallido.
    - `shipments.reserved` (entrada): envío reservado.
    - `shipments.failed` (entrada): envío fallido.
    - `payments.compensate` (salida): solicitar compensación de pago.

- Puerto HTTP: 8080

## Diseño

- Coreografía: el coordinador no invoca servicios, solo observa y publica
  eventos.
- Estado en memoria: se almacena un `Map<orderId, status>` para simplificar.
- Compensación: si falla el envío, se emite `CompensatePayment` para revertir el
  cobro. Si falla el pago, se da por compensado.
