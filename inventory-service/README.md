# Inventory Service

Stock level management service with reservation and release capabilities.

## Overview

The Inventory Service manages product stock levels with support for stock reservation and release operations. This is useful for order processing workflows where stock needs to be temporarily reserved.

## Running the Service

```bash
cd inventory-service
mvn spring-boot:run
```

The service will start on port **8084**.

## API Endpoints

### Base URL: `http://localhost:8084/api/inventory`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/` | Get all inventory items | None |
| GET | `/{productId}` | Get inventory for product | None |
| GET | `/{productId}/available?quantity={qty}` | Check stock availability | None |
| GET | `/available` | Get items with available stock | None |
| GET | `/low-stock?threshold={num}` | Get low stock items | None |
| POST | `/` | Create inventory item | InventoryItem JSON |
| PUT | `/{productId}?stock={amount}` | Update stock level | None |
| POST | `/{productId}/reserve` | Reserve stock | Quantity JSON |
| POST | `/{productId}/release` | Release reserved stock | Quantity JSON |
| DELETE | `/{productId}` | Delete inventory item | None |
| GET | `/health` | Health check | None |

### Inventory Item Model

```json
{
  "id": 1,
  "productId": 1,
  "availableStock": 45,
  "reservedStock": 5,
  "lastUpdated": "2023-12-01T10:00:00"
}
```

### Stock Reservation Request

```json
{
  "quantity": 3
}
```

### Validation Rules

- `productId`: Required, unique per inventory item
- `availableStock`: Must be >= 0
- `reservedStock`: Must be >= 0
- `quantity` (for reservations): Must be >= 1

## Sample API Calls

### Create Inventory Item

```bash
curl -X POST http://localhost:8084/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "availableStock": 100
  }'
```

### Get All Inventory

```bash
curl http://localhost:8084/api/inventory
```

### Get Inventory for Product

```bash
curl http://localhost:8084/api/inventory/1
```

### Check Stock Availability

```bash
curl "http://localhost:8084/api/inventory/1/available?quantity=5"
```

### Update Stock Level

```bash
curl -X PUT "http://localhost:8084/api/inventory/1?stock=150"
```

### Reserve Stock

```bash
curl -X POST http://localhost:8084/api/inventory/1/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5
  }'
```

### Release Reserved Stock

```bash
curl -X POST http://localhost:8084/api/inventory/1/release \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3
  }'
```

### Get Low Stock Items

```bash
curl "http://localhost:8084/api/inventory/low-stock?threshold=10"
```

### Delete Inventory Item

```bash
curl -X DELETE http://localhost:8084/api/inventory/1
```

## Stock Management Logic

### Available vs Reserved Stock

- **Available Stock**: Stock that can be sold/reserved
- **Reserved Stock**: Stock temporarily held (e.g., during order processing)
- **Total Stock**: Available + Reserved

### Reservation Process

1. Check if sufficient available stock exists
2. Move stock from available to reserved
3. Stock remains reserved until released or order completed

### Release Process

1. Move stock from reserved back to available
2. Used when orders are cancelled or reservations expire

## Database

- **Type**: H2 In-Memory Database
- **Console**: http://localhost:8084/h2-console
- **JDBC URL**: `jdbc:h2:mem:inventorydb`
- **Username**: `sa`
- **Password**: (empty)

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: Inventory item created successfully
- `400 Bad Request`: Validation errors or insufficient stock
- `404 Not Found`: Inventory item not found
- `500 Internal Server Error`: Server error

### Error Response Format

```json
{
  "error": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock for product 1. Available: 3, Requested: 5",
  "timestamp": "2023-12-01T10:00:00"
}
```

## Use Cases

### E-commerce Order Processing

1. **Order Creation**: Reserve stock for order items
2. **Payment Processing**: Keep stock reserved during payment
3. **Order Confirmation**: Convert reserved stock to sold (remove from inventory)
4. **Order Cancellation**: Release reserved stock back to available

### Inventory Monitoring

1. **Low Stock Alerts**: Monitor items below threshold
2. **Stock Availability**: Check before allowing orders
3. **Inventory Reports**: Track available vs reserved stock

## Testing

Run unit tests:

```bash
mvn test
```

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- H2 Database
- Spring Boot Starter Test