# API Documentation

This document provides detailed information about the REST APIs exposed by each microservice.

## API Gateway

The API Gateway serves as the single entry point for all client requests and routes them to the appropriate microservices.

**Base URL**: `http://localhost:8080`

### Routing Rules

| Path Pattern | Target Service | Target URL |
|-------------|----------------|------------|
| `/users/**` | User Service | `http://localhost:8084` |
| `/products/**` | Product Service | `http://localhost:8083` |
| `/inventory/**` | Inventory Service | `http://localhost:8081` |
| `/orders/**` | Order Service | `http://localhost:8082` |

## User Service API

**Base URL**: `http://localhost:8084/api`

### Endpoints

#### Get All Users
```http
GET /api/users
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

#### Get User by ID
```http
GET /api/users/{id}
```

**Parameters**:
- `id` (path) - User ID

**Response**:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Create User
```http
POST /api/users
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Jane Smith",
  "email": "jane.smith@example.com"
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

#### Update User
```http
PUT /api/users/{id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com"
}
```

**Response**:
```json
{
  "id": 2,
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

#### Delete User
```http
DELETE /api/users/{id}
```

**Response**: 204 No Content

## Product Service API

**Base URL**: `http://localhost:8083/api`

### Endpoints

#### Get All Products
```http
GET /api/products
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "category": "Electronics",
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

#### Get Product by ID
```http
GET /api/products/{id}
```

**Response**:
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "category": "Electronics",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Create Product
```http
POST /api/products
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Smartphone",
  "description": "Latest smartphone model",
  "price": 699.99,
  "category": "Electronics"
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "name": "Smartphone",
  "description": "Latest smartphone model",
  "price": 699.99,
  "category": "Electronics",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

#### Update Product
```http
PUT /api/products/{id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1299.99,
  "category": "Electronics"
}
```

#### Delete Product
```http
DELETE /api/products/{id}
```

**Response**: 204 No Content

## Inventory Service API

**Base URL**: `http://localhost:8081/api`

### Endpoints

#### Get All Inventory Items
```http
GET /api/inventory
```

**Response**:
```json
[
  {
    "id": 1,
    "productId": 1,
    "quantity": 50,
    "reservedQuantity": 5,
    "availableQuantity": 45,
    "lastUpdated": "2024-01-15T10:30:00Z"
  }
]
```

#### Get Inventory by Product ID
```http
GET /api/inventory/{productId}
```

**Response**:
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 50,
  "reservedQuantity": 5,
  "availableQuantity": 45,
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

#### Create Inventory Item
```http
POST /api/inventory
Content-Type: application/json
```

**Request Body**:
```json
{
  "productId": 2,
  "quantity": 100
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "productId": 2,
  "quantity": 100,
  "reservedQuantity": 0,
  "availableQuantity": 100,
  "lastUpdated": "2024-01-15T11:00:00Z"
}
```

#### Update Inventory
```http
PUT /api/inventory/{id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "quantity": 75,
  "reservedQuantity": 10
}
```

#### Delete Inventory Item
```http
DELETE /api/inventory/{id}
```

**Response**: 204 No Content

## Order Service API

**Base URL**: `http://localhost:8082/api`

### Endpoints

#### Get All Orders
```http
GET /api/orders
```

**Response**:
```json
[
  {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 999.99
      }
    ],
    "totalAmount": 1999.98,
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

#### Get Order by ID
```http
GET /api/orders/{id}
```

**Response**:
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 999.99
    }
  ],
  "totalAmount": 1999.98,
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Create Order
```http
POST /api/orders
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "price": 999.99
    },
    {
      "productId": 2,
      "quantity": 2,
      "price": 699.99
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "price": 999.99
    },
    {
      "productId": 2,
      "quantity": 2,
      "price": 699.99
    }
  ],
  "totalAmount": 2399.97,
  "status": "PENDING",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

#### Update Order
```http
PUT /api/orders/{id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "status": "CONFIRMED"
}
```

#### Delete Order
```http
DELETE /api/orders/{id}
```

**Response**: 204 No Content

## Error Responses

All services follow consistent error response format:

### Validation Error (400 Bad Request)
```json
{
  "timestamp": "2024-01-15T11:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users",
  "details": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

### Not Found (404 Not Found)
```json
{
  "timestamp": "2024-01-15T11:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999"
}
```

### Internal Server Error (500 Internal Server Error)
```json
{
  "timestamp": "2024-01-15T11:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/users"
}
```

## HTTP Status Codes

| Status Code | Description |
|------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request successful, no content returned |
| 400 | Bad Request - Invalid request data |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error |

## Rate Limiting

The API Gateway implements rate limiting:
- 100 requests per minute per IP address
- 1000 requests per hour per authenticated user

Rate limit headers are included in responses:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248000
```

## Authentication

Currently, the services use basic authentication. Future enhancements will include:
- JWT token-based authentication
- OAuth 2.0 integration
- API key authentication

## Versioning

API versioning strategy:
- URL versioning: `/api/v1/users`
- Header versioning: `Accept: application/vnd.api+json;version=1`

## CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured to allow:
- Origins: `http://localhost:3000`, `http://localhost:4200`
- Methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- Headers: `Content-Type`, `Authorization`

## Testing with Postman

Import the provided `postman-collection.json` file to test all endpoints with pre-configured requests and environment variables.