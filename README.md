# Spring Boot Microservices Sample

A comprehensive microservices architecture built with Spring Boot, demonstrating best practices for distributed systems including service communication, containerization, and API management.

## Architecture Overview

This project consists of 5 microservices:

- **API Gateway** (Port 8080) - Entry point for all client requests
- **User Service** (Port 8084) - User management and authentication
- **Product Service** (Port 8083) - Product catalog management
- **Inventory Service** (Port 8081) - Stock and inventory tracking
- **Order Service** (Port 8082) - Order processing and management

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Database**: H2 (in-memory for development)
- **Build Tool**: Maven
- **Containerization**: Docker
- **API Documentation**: Postman Collection included

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional, for containerized deployment)

### Running Locally

1. Clone the repository
```bash
git clone https://github.com/pavanyn2002/microservices_devops-practice.git
cd spring-boot-microservices-sample
```

2. Start each service (in separate terminals):
```bash
# API Gateway
cd api-gateway && mvn spring-boot:run

# User Service
cd user-service && mvn spring-boot:run

# Product Service
cd product-service && mvn spring-boot:run

# Inventory Service
cd inventory-service && mvn spring-boot:run

# Order Service
cd order-service && mvn spring-boot:run
```

3. Access the services:
- API Gateway: http://localhost:8080
- User Service: http://localhost:8084
- Product Service: http://localhost:8083
- Inventory Service: http://localhost:8081
- Order Service: http://localhost:8082

### Running with Docker

1. Build all services:
```bash
docker build -t api-gateway ./api-gateway
docker build -t user-service ./user-service
docker build -t product-service ./product-service
docker build -t inventory-service ./inventory-service
docker build -t order-service ./order-service
```

2. Run services:
```bash
docker run -p 8080:8080 api-gateway
docker run -p 8084:8084 user-service
docker run -p 8083:8083 product-service
docker run -p 8081:8081 inventory-service
docker run -p 8082:8082 order-service
```

## API Documentation

### User Service Endpoints

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Service Endpoints

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Inventory Service Endpoints

- `GET /api/inventory` - Get all inventory items
- `GET /api/inventory/{productId}` - Get inventory for product
- `POST /api/inventory` - Create inventory item
- `PUT /api/inventory/{id}` - Update inventory
- `DELETE /api/inventory/{id}` - Delete inventory item

### Order Service Endpoints

- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Delete order

### API Gateway Routes

All requests should go through the API Gateway at `http://localhost:8080`:

- `/users/**` → User Service
- `/products/**` → Product Service
- `/inventory/**` → Inventory Service
- `/orders/**` → Order Service

## Testing

### Using Postman

Import the `postman-collection.json` file into Postman to test all endpoints with pre-configured requests.

### Running Unit Tests

```bash
# Test all services
mvn test

# Test specific service
cd user-service && mvn test
```

## Project Structure

```
spring-boot-microservices-sample/
├── api-gateway/
│   ├── src/main/java/com/example/gateway/
│   ├── Dockerfile
│   └── pom.xml
├── user-service/
│   ├── src/main/java/com/example/user/
│   ├── Dockerfile
│   └── pom.xml
├── product-service/
│   ├── src/main/java/com/example/product/
│   ├── Dockerfile
│   └── pom.xml
├── inventory-service/
│   ├── src/main/java/com/example/inventory/
│   ├── Dockerfile
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/example/order/
│   ├── Dockerfile
│   └── pom.xml
├── docs/
├── postman-collection.json
└── README.md
```

## Configuration

Each service uses Spring Boot's default configuration with H2 database. Configuration files are located in `src/main/resources/application.properties` for each service.

## Development Guidelines

- Each service is independently deployable
- Services communicate via HTTP REST APIs
- All requests go through the API Gateway
- H2 database provides data persistence (development mode)
- Follow RESTful API conventions
- Use proper HTTP status codes
- Implement proper error handling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues, please create an issue in the repository or contact the development team.