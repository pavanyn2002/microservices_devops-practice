# API Gateway

Single entry point for client requests with routing to backend microservices.

## Overview

The API Gateway provides a unified interface for accessing all microservices. It routes requests to appropriate backend services and includes health monitoring for all services.

## Running the Service

**Prerequisites**: All backend services should be running for full functionality.

```bash
cd api-gateway
mvn spring-boot:run
```

The service will start on port **8080**.

## Routing Configuration

The gateway routes requests based on URL patterns:

| Route Pattern | Target Service | Port |
|---------------|----------------|------|
| `/api/users/**` | User Service | 8081 |
| `/api/products/**` | Product Service | 8082 |
| `/api/orders/**` | Order Service | 8083 |
| `/api/inventory/**` | Inventory Service | 8084 |

## API Endpoints

### Gateway Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Gateway information |
| GET | `/health` | Health check for gateway and all services |

### Proxied Service Endpoints

All service endpoints are available through the gateway by prefixing with the gateway URL:

```
Direct:  http://localhost:8081/api/users
Gateway: http://localhost:8080/api/users

Direct:  http://localhost:8082/api/products
Gateway: http://localhost:8080/api/products

Direct:  http://localhost:8083/api/orders
Gateway: http://localhost:8080/api/orders

Direct:  http://localhost:8084/api/inventory
Gateway: http://localhost:8080/api/inventory
```

## Sample API Calls

### Gateway Health Check

```bash
curl http://localhost:8080/health
```

Response:
```json
{
  "gateway": "UP",
  "services": {
    "user-service": "UP",
    "product-service": "UP",
    "order-service": "UP",
    "inventory-service": "UP"
  }
}
```

### Create User via Gateway

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Get Products via Gateway

```bash
curl http://localhost:8080/api/products
```

### Create Order via Gateway

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

## Features

### Request Forwarding

- Preserves HTTP method (GET, POST, PUT, DELETE)
- Forwards request headers
- Forwards request body for POST/PUT requests
- Forwards query parameters

### Error Handling

- Returns 503 Service Unavailable when backend services are down
- Provides meaningful error messages
- Logs all routing attempts for debugging

### Health Monitoring

- Monitors all backend services
- Provides aggregated health status
- Individual service health status

## Configuration

Service URLs are configured in `application.yml`:

```yaml
services:
  user-service: http://localhost:8081
  product-service: http://localhost:8082
  order-service: http://localhost:8083
  inventory-service: http://localhost:8084
```

## Error Handling

### Service Unavailable

When a backend service is unavailable:

```json
{
  "error": "SERVICE_UNAVAILABLE",
  "message": "Backend service is currently unavailable",
  "timestamp": "2023-12-01T10:00:00"
}
```

### Gateway Error

For gateway-specific errors:

```json
{
  "error": "GATEWAY_ERROR",
  "message": "An error occurred while processing the request",
  "timestamp": "2023-12-01T10:00:00"
}
```

## Benefits

### For Clients

- **Single Entry Point**: One URL to remember
- **Simplified Configuration**: No need to know individual service ports
- **Consistent Interface**: Uniform error handling and response format

### For Operations

- **Centralized Monitoring**: Single point to monitor all services
- **Load Balancing**: Can be extended with load balancing logic
- **Security**: Single point for authentication/authorization (future enhancement)
- **Rate Limiting**: Can implement rate limiting (future enhancement)

## Logging

The gateway logs all requests:

```
DEBUG - Forwarding POST request to User Service: http://localhost:8081/api/users
DEBUG - Successfully forwarded request to User Service, status: 201 CREATED
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Testing

1. Start all backend services
2. Start the gateway
3. Test routing through gateway endpoints
4. Verify responses match direct service calls

## Future Enhancements

The gateway can be extended with:

- **Authentication/Authorization**: JWT token validation
- **Rate Limiting**: Request throttling per client
- **Load Balancing**: Multiple instances of backend services
- **Circuit Breaker**: Fail-fast when services are down
- **Request/Response Transformation**: Modify requests/responses
- **Caching**: Cache frequently requested data
- **Metrics**: Request counting and timing

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Test
- RestTemplate for service communication