# User Service

User account management service providing CRUD operations for user data.

## Overview

The User Service manages user accounts and provides REST endpoints for user operations. It uses H2 in-memory database for data persistence and includes validation for user data.

## Running the Service

```bash
cd user-service
mvn spring-boot:run
```

The service will start on port **8081**.

## API Endpoints

### Base URL: `http://localhost:8081/api/users`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/` | Get all users | None |
| GET | `/{id}` | Get user by ID | None |
| POST | `/` | Create new user | User JSON |
| PUT | `/{id}` | Update user | User JSON |
| DELETE | `/{id}` | Delete user | None |
| GET | `/health` | Health check | None |

### User Model

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Validation Rules

- `username`: Required, unique
- `email`: Required, valid email format, unique
- `firstName`: Required
- `lastName`: Required

## Sample API Calls

### Create User

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Get All Users

```bash
curl http://localhost:8081/api/users
```

### Get User by ID

```bash
curl http://localhost:8081/api/users/1
```

### Update User

```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johnsmith",
    "email": "johnsmith@example.com",
    "firstName": "John",
    "lastName": "Smith"
  }'
```

### Delete User

```bash
curl -X DELETE http://localhost:8081/api/users/1
```

## Database

- **Type**: H2 In-Memory Database
- **Console**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:mem:userdb`
- **Username**: `sa`
- **Password**: (empty)

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: User created successfully
- `400 Bad Request`: Validation errors
- `404 Not Found`: User not found
- `500 Internal Server Error`: Server error

### Error Response Format

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Username already exists: johndoe",
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