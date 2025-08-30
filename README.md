# Coreografía SAGA para pedidos de usuario (Java 21 + Spring Cloud Stream)

Este repositorio contiene un ejemplo funcional de la **patrón SAGA en
coreografía** para procesar pedidos de usuario, dividido en tres microservicios:

- payment-service: gestiona pagos.
- transport-service: gestiona el transporte/envío.
- order-coordinator: inicia el pedido y coordina las respuestas vía eventos (sin
  llamadas directas).

Toda la comunicación es asíncrona sobre Kafka (usando Redpanda para
simplificar), implementada con Spring Cloud Stream y funciones (
supplier/consumer).

## Requisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose (para levantar Kafka/Redpanda)

## Levantar infraestructura (Kafka compatible)

```bash
docker compose up -d redpanda
```

Esto levanta Redpanda exponiendo el broker en `localhost:19092` y
`redpanda:9092` dentro de la red de Docker.

## Construir imágenes Docker (Dockerfile)

Debido a problemas con el comando spring-boot:build-image, ahora se incluyen
Dockerfiles por servicio. El flujo recomendado es:

1) Construir los JARs con Maven (una sola vez):

```bash
mvn -q -DskipTests clean package
```

2) Construir las imágenes con Docker Compose (usa los Dockerfile de cada
   módulo):

```bash
docker compose build
```

Esto creará las imágenes con los nombres:
- com.example.saga/order-coordinator:1.0.0-SNAPSHOT
- com.example.saga/payment-service:1.0.0-SNAPSHOT
- com.example.saga/transport-service:1.0.0-SNAPSHOT

Nota: Si prefieres construir individualmente:

```bash
# Desde cada módulo (tras mvn package)
docker build -t com.example.saga/order-coordinator:1.0.0-SNAPSHOT -f order-coordinator/Dockerfile order-coordinator

docker build -t com.example.saga/payment-service:1.0.0-SNAPSHOT -f payment-service/Dockerfile payment-service

docker build -t com.example.saga/transport-service:1.0.0-SNAPSHOT -f transport-service/Dockerfile transport-service
```

## Ejecutar con Docker Compose

Una vez construidas las imágenes, puedes levantar todos los servicios junto con
Redpanda:

```bash
docker compose up -d
```

El `order-coordinator` expone el puerto `8080` en tu máquina.

## Compilar y ejecutar sin Docker

Compilar todo:

```bash
mvn -q -DskipTests clean package
```

En terminales separadas, ejecutar cada servicio:

```bash
# Coordinador del pedido
mvn -q -pl order-coordinator -am spring-boot:run

# Servicio de pagos
mvn -q -pl payment-service -am spring-boot:run

# Servicio de transporte
mvn -q -pl transport-service -am spring-boot:run
```

## Flujo de eventos (coreografía)

1. order-coordinator publica `OrderCreated` en el tópico `orders` cuando se
   llama a su endpoint HTTP `/api/orders`.
2. payment-service consume `OrderCreated` y responde con `PaymentAuthorized` o
   `PaymentFailed` en `payments`.
3. transport-service consume `PaymentAuthorized` y responde con
   `ShipmentReserved` o `ShipmentFailed` en `shipments`.
4. order-coordinator escucha `payments` y `shipments` para cerrar la saga:
    - si todo va bien -> `OrderCompleted`.
    - si falla pago -> `OrderCompensated` (cancelación).
    - si falla transporte -> emite `CompensatePayment` y, tras revertir el pago,
      `OrderCompensated`.

La correlación se realiza por `orderId` (UUID). Todos los eventos incluyen
`orderId` y `eventType`.

## Pruebas

Incluimos una prueba mínima en `order-coordinator` usando el test binder de
Spring Cloud Stream para validar el flujo básico de transición de estados.

## Módulos

- order-coordinator: expone un API REST para crear órdenes y coordina la saga
  por eventos.
- payment-service: simula autorización de pago (éxito/fallo configurable).
- transport-service: simula reserva de envío (éxito/fallo configurable).

Cada módulo tiene su propio README en español con instrucciones y detalles.

## Notas

- Este ejemplo usa coreografía (no hay orquestador central llamado
  directamente). El coordinador solo observa y publica eventos para mantener el
  estado del pedido y solicitar compensaciones cuando corresponde.
- Broker Kafka: Redpanda (Docker). Puedes sustituir por Kafka si lo prefieres
  ajustando `spring.cloud.stream.kafka.binder.brokers`.
