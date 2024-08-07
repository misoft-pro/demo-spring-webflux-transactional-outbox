# This repository demonstrates a possible implementation of the Transactional Outbox pattern in Spring Boot WebFlux app with R2DBC.

Demonstrated Spring Boot app it's a Payment Gateway Service which accepts payments from the merchants. The main responsibility of this service is to manage API contracts and process payments. Payment Gateway it's a Reactive API server with a high throughput and low latency utilizing Spring Boot WebFlux with Kotlin Coroutines. Payment Gateway does an efficient non-blocking downstream calls and returns a result to the client once an async operation is completed.

Business scope:

The business requirement is defined as "Merchant must be able to place a payment request and receive a payment processing result on the merchant_callback_url. Payment request can be in any supported currencies selected by merchant during configuration. Payment flow should support both 3DS and non-3DS transactions". See the [payment flow sequence diagram](documentation/diagrams/3d-secure-payment-flow.plantuml).

Technical scope:

- [ ] Transactional Outbox pattern implementation for R2DBC.
- [ ] Spring Boot WebFlux API Server: Utilizing Kotlin and Coroutines for handling all asynchronous operations.
- [ ] App Containerization: Efficient packaging and deployment of the application.
- [ ] Logging: Comprehensive logging mechanisms for tracking and debugging.
- [ ] Distributed Tracing: Implementing tracing to monitor and troubleshoot distributed systems.
- [ ] Metrics and Health Checks: Tools to ensure system health and performance monitoring.
- [ ] Autogenerated OpenAPI Documentation: Interactive API documentation with Swagger UI.
- [ ] Error Handling: Standardized error responses with ApiError objects for all backend exceptions.
- [ ] Testing: Robust testing practices to ensure reliability and performance.44


## Tech stack:

- [ ] [Kotlin 2.0.0](https://kotlinlang.org/docs/getting-started.html#install-kotlin)
  and [coroutines](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/)
- [ ] [Spring Boot WebFlux 3.2.5](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [ ] [Kotlin Coroutines Reactor](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-reactor/) (see
  also [Spring coroutines guide](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html) on
  how Reactive translates to Coroutines).

## Runtime requirements

- JDK 21+
- Docker

## Compile and package application (tests run included)

```bash
./mvnw clean package
```

## Only test run

```bash
./mvnw clean test
```

## Build docker image

```bash
docker build -t api-server .
```

## Run docker image

```bash
docker run -d -p 8080:8080 --name api-server api-server
```

## How to guarantee Consistency in a Distributed System using Transactional Outbox pattern?

A Payment Gateway command creates a transaction record in the database and send messages/events to a downstream service/message broker. The command must atomically update the database and send messages in order to avoid data inconsistencies and bugs. However, from the high availability perspective it is not viable to use a traditional distributed transaction (2PC) that spans the database and the message broker, and the database and/or the message broker might not support 2PC. But without using 2PC, sending a message in the middle of a transaction is not reliable. There’s no guarantee that the transaction will commit. Similarly, if a service sends a message after committing the transaction there’s no guarantee that it won’t crash before sending the message.

So how to atomically update the database and send messages to another service/message broker?
The solution is for the service that sends the message to first store the message in the database as part of the transaction that updates the business entities. A separate process then sends the messages to the message broker.


## Logging

For the moment all server logs are written to the console and file using `ch.qos.logback.core.ConsoleAppender`
and `ch.qos.logback.core.rolling.RollingFileAppender` respectively configured in `logback.xml` file. All logs contain `traceId` value
which is implicitly populated from `org.slf4j.MDC` context and shown in the logs according to `CONSOLE_LOG_PATTERN` defined in
logback.xml.

Log record example with traceId printed right after log level `INFO`:

`2024-05-24 12:55:16.986  INFO [2695f8537e1fe05a841f0df18898e730] 1612 - [          parallel-1]  c.e.a.w.c.PaymentController .placePayment(40) : Place payment request`

## Tracing

Distributed `traceId` is attached to every incoming request and automatically propagated to downstream threads and
requests.
Downstream treads/coroutines can access it through implicitly propagated context implemented by `micrometer-tracing`
library.

All API responses contain `X-Trace-Id` header to be able to match every http request with corresponding logs on the server side.
Example of http response header `X-Trace-Id: 7e0674227780f3226ae9a8b7d350a5ee`.

## Metrics

All maintenance endpoints are accessed by following url `http://localhost:8080/api/internal/actuator`. The list of all
app measured metrics are here `http://localhost:8080/api/internal/actuator/metrics`. For example, the number of API
calls since server start is exposed in Prometheus format
by a link `http://localhost:8080/api/internal/actuator/metrics/custom.api.calls.total` and implemented
using `io.micrometer.core.instrument.Counter` from Micrometer library.

## Health checks

Health checks are provided through Spring Boot Actuator by a link http://localhost:8080/api/internal/actuator/health

## Error handling

All thrown exceptions are handled globally by using Spring
@ControllerAdvice at class `pro.misoft.apiservice.web.errorhandling.RestExceptionHandler`. This exception handler convert exception to http response with proper http code and error body. Error body has the localized error message to be shown to the end user and unique internal code to be used by customer support team. Error body json:
```
{
   “httpStatus”: 400,
   “internalCode":"order-4002",
   “errorMessage":"Input fields contain errors",
   "traceId":"7f006775-04b5-4f81-8250-a85ffb976722",
   "subErrors":[
      {
         "objectName":"orderDto",
         "fieldName":"userName”,
         "rejectedValue”:”N”,
         "message":"size must be between 2 and 36"
      }
   ]
}
```
Error body data class:
```
data class ApiError(
 val httpStatus: Int,
    /**
     * Internal code to classify error
     *
     * pattern="${serviceNamePrefix}-${httpErrorCategory}${sequenceNumberUniqueForServiceNameAndHttpErrorCode}".
     *
     * examples=["apiservice-4001", "user-4001", "user-4002", "user-5001"]
     */
    val internalCode: String,
    /**
     * Human-readable localized message to display on client side
     */
    val errorMessage: String,
    /**
     * Unique identifier of user request.
     * In case of distributed architecture this identifier is passed to all downstream requests to other services.
     */
    val traceId: String,
    /**
     * Collect information about sub errors,
     * for example specific fields of forms providing human-readable error messages for each field to guide user trough out a flow
     */
    val subErrors: List<ApiSubError> = listOf()
   )
```

## Openapi documentation

`Springdoc-openapi` library is integrated to automatically generate OpenAPI documentation. Endpoint to see OpenAPI spec http://localhost:8080/api/internal/openapi. Swagger-UI is already embedded to web server and can be accessed by url http://localhost:8080/api/internal/swagger-ui. The openapi contract schema can be customized by applying swagger annotations like `io.swagger.v3.oas.annotations.media.Schema` to the data classes.

## API usage

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
  "requestId": "request123456789",
  "merchantId": "merchant123456789",
  "merchantRedirectUrl": "https://merchant.com/redirect",
  "card": {
    "number": "4111******111111",
    "expiryDate": "12/25",
    "cardToken": "abcdef123456"
  },
  "order": {
    "id": "order-1234",
    "total": {
      "amount": "100.00",
      "currency": "USD"
    }
  }
}'  
```