# Product Service

Product catalog management service providing CRUD operations and category filtering for products.

## Overview

The Product Service manages the product catalog with support for categories, pricing, and product search functionality. It uses H2 in-memory database for data persistence.

## Running the Service

```bash
cd product-service
mvn spring-boot:run
```

The service will start on port **8082**.

## API Endpoints

### Base URL: `http://localhost:8082/api/products`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/` | Get all products | None |
| GET | `/{id}` | Get product by ID | None |
| GET | `/category/{category}` | Get products by category | None |
| GET | `/search?name={name}` | Search products by name | None |
| POST | `/` | Create new product | Product JSON |
| PUT | `/{id}` | Update product | Product JSON |
| DELETE | `/{id}` | Delete product | None |
| GET | `/health` | Health check | None |

### Product Model

```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1299.99,
  "category": "Electronics",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Validation Rules

- `name`: Required
- `price`: Required, must be >= 0
- `description`: Optional
- `category`: Optional

## Sample API Calls

### Create Product

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "category": "Electronics"
  }'
```

### Get All Products

```bash
curl http://localhost:8082/api/products
```

### Get Products by Category

```bash
curl http://localhost:8082/api/products/category/Electronics
```

### Search Products by Name

```bash
curl "http://localhost:8082/api/products/search?name=laptop"
```

### Update Product

```bash
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Gaming Laptop",
    "description": "Updated description",
    "price": 1199.99,
    "category": "Electronics"
  }'
```

### Delete Product

```bash
curl -X DELETE http://localhost:8082/api/products/1
```

## Database

- **Type**: H2 In-Memory Database
- **Console**: http://localhost:8082/h2-console
- **JDBC URL**: `jdbc:h2:mem:productdb`
- **Username**: `sa`
- **Password**: (empty)

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: Product created successfully
- `400 Bad Request`: Validation errors
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Server error

### Error Response Format

```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product not found with id: 1",
  "timestamp": "2023-12-01T10:00:00"
}
```

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