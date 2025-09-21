# Order Service

Order management service with user and product validation through inter-service communication.

## Overview

The Order Service handles order creation and management. It validates users and products by communicating with the User Service and Product Service respectively. This demonstrates microservices communication patterns.

## Running the Service

**Prerequisites**: User Service and Product Service must be running first.

```bash
cd order-service
mvn spring-boot:run
```

The service will start on port **8083**.

## API Endpoints

### Base URL: `http://localhost:8083/api/orders`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/` | Get all orders | None |
| GET | `/{id}` | Get order by ID | None |
| GET | `/user/{userId}` | Get orders by user ID | None |
| POST | `/` | Create new order | Order JSON |
| PUT | `/{id}?status={status}` | Update order status | None |
| DELETE | `/{id}` | Delete order | None |
| GET | `/health` | Health check | None |

### Order Model

```json
{
  "id": 1,
  "userId": 1,
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99
    }
  ],
  "status": "PENDING",
  "totalAmount": 1999.98,
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Order Status Values

- `PENDING`: Initial status
- `CONFIRMED`: Order confirmed
- `PROCESSING`: Being processed
- `SHIPPED`: Order shipped
- `DELIVERED`: Order delivered
- `CANCELLED`: Order cancelled

### Validation Rules

- `userId`: Required, must exist in User Service
- `orderItems`: Required, at least one item
- `orderItems[].productId`: Required, must exist in Product Service
- `orderItems[].quantity`: Required, minimum 1

## Sample API Calls

### Create Order

```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

### Get All Orders

```bash
curl http://localhost:8083/api/orders
```

### Get Orders by User

```bash
curl http://localhost:8083/api/orders/user/1
```

### Update Order Status

```bash
curl -X PUT "http://localhost:8083/api/orders/1?status=CONFIRMED"
```

### Delete Order

```bash
curl -X DELETE http://localhost:8083/api/orders/1
```

## Inter-Service Communication

The Order Service communicates with:

### User Service
- **Endpoint**: `http://localhost:8081/api/users/{id}`
- **Purpose**: Validate user exists before creating order
- **Fallback**: Returns validation error if user service unavailable

### Product Service
- **Endpoint**: `http://localhost:8082/api/products/{id}`
- **Purpose**: Validate products exist and get current pricing
- **Fallback**: Returns validation error if product service unavailable

## Database

- **Type**: H2 In-Memory Database
- **Console**: http://localhost:8083/h2-console
- **JDBC URL**: `jdbc:h2:mem:orderdb`
- **Username**: `sa`
- **Password**: (empty)

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: Order created successfully
- `400 Bad Request`: Validation errors
- `404 Not Found`: Order not found
- `503 Service Unavailable`: External service unavailable
- `500 Internal Server Error`: Server error

### Error Response Format

```json
{
  "error": "VALIDATION_ERROR",
  "message": "User not found with id: 999",
  "timestamp": "2023-12-01T10:00:00"
}
```

## Configuration

Service URLs are configured in `application.yml`:

```yaml
services:
  user-service: http://localhost:8081
  product-service: http://localhost:8082
```

## Testing

Run unit tests:

```bash
mvn test
```

**Note**: Tests use mocked external service clients.

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- H2 Database
- Spring Boot Starter Test
- RestTemplate for service communication