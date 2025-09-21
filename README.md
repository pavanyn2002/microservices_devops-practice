# Spring Boot Microservices Sample Application

A sample microservices-based application using Spring Boot with 5 independent services designed for DevOps practice. This application demonstrates core microservices patterns including service communication, API gateway routing, and independent data management.

## Services Overview

- **API Gateway** (Port 8080) - Single entry point for client requests with routing to backend services
- **User Service** (Port 8081) - User account management (CRUD operations)
- **Product Service** (Port 8082) - Product catalog management with category filtering
- **Order Service** (Port 8083) - Order creation and management with user/product validation
- **Inventory Service** (Port 8084) - Stock level management with reservation capabilities

## Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Git** (for cloning the repository)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd spring-microservices-sample

# Build all services
mvn clean install -f user-service/pom.xml
mvn clean install -f product-service/pom.xml
mvn clean install -f order-service/pom.xml
mvn clean install -f inventory-service/pom.xml
mvn clean install -f api-gateway/pom.xml
```

### 2. Start Services (Recommended Order)

Start services in separate terminals in this order to ensure proper inter-service communication:

```bash
# Terminal 1 - User Service (required by Order Service)
cd user-service
mvn spring-boot:run

# Terminal 2 - Product Service (required by Order Service)
cd product-service
mvn spring-boot:run

# Terminal 3 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 4 - Order Service (depends on User and Product services)
cd order-service
mvn spring-boot:run

# Terminal 5 - API Gateway (routes to all services)
cd api-gateway
mvn spring-boot:run
```

### 3. Verify Services are Running

Check that all services are healthy:

```bash
# Individual service health checks
curl http://localhost:8081/api/users/health
curl http://localhost:8082/api/products/health
curl http://localhost:8083/api/orders/health
curl http://localhost:8084/api/inventory/health

# Gateway health check (includes all services)
curl http://localhost:8080/health
```

## API Endpoints

### Via API Gateway (Recommended)

All services can be accessed through the API Gateway at `http://localhost:8080`:

```bash
# Users
GET    http://localhost:8080/api/users
POST   http://localhost:8080/api/users
GET    http://localhost:8080/api/users/{id}
PUT    http://localhost:8080/api/users/{id}
DELETE http://localhost:8080/api/users/{id}

# Products
GET    http://localhost:8080/api/products
POST   http://localhost:8080/api/products
GET    http://localhost:8080/api/products/{id}
GET    http://localhost:8080/api/products/category/{category}
PUT    http://localhost:8080/api/products/{id}
DELETE http://localhost:8080/api/products/{id}

# Orders
GET    http://localhost:8080/api/orders
POST   http://localhost:8080/api/orders
GET    http://localhost:8080/api/orders/{id}
GET    http://localhost:8080/api/orders/user/{userId}
PUT    http://localhost:8080/api/orders/{id}?status=CONFIRMED
DELETE http://localhost:8080/api/orders/{id}

# Inventory
GET    http://localhost:8080/api/inventory
POST   http://localhost:8080/api/inventory
GET    http://localhost:8080/api/inventory/{productId}
PUT    http://localhost:8080/api/inventory/{productId}?stock=100
POST   http://localhost:8080/api/inventory/{productId}/reserve
POST   http://localhost:8080/api/inventory/{productId}/release
DELETE http://localhost:8080/api/inventory/{productId}
```

### Direct Service Access

Services can also be accessed directly:

- **User Service**: http://localhost:8081/api/users
- **Product Service**: http://localhost:8082/api/products
- **Order Service**: http://localhost:8083/api/orders
- **Inventory Service**: http://localhost:8084/api/inventory

## Sample API Usage

### 1. Create a User

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

### 2. Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming laptop",
    "price": 999.99,
    "category": "Electronics"
  }'
```

### 3. Add Inventory

```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "availableStock": 50
  }'
```

### 4. Create an Order

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

## Database Access

Each service uses H2 in-memory database with web console access:

- **User Service**: http://localhost:8081/h2-console
- **Product Service**: http://localhost:8082/h2-console
- **Order Service**: http://localhost:8083/h2-console
- **Inventory Service**: http://localhost:8084/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:[servicename]db` (e.g., `jdbc:h2:mem:userdb`)
- Username: `sa`
- Password: (leave empty)

## Architecture Details

### Service Communication

- **Synchronous Communication**: REST APIs over HTTP
- **Order Service Integration**: Validates users via User Service and products via Product Service
- **Error Handling**: Circuit breaker pattern with fallback responses
- **Data Consistency**: Each service owns its data with eventual consistency

### Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **Communication**: REST APIs with RestTemplate

## Testing

### Unit Tests

Run tests for individual services:

```bash
cd user-service && mvn test
cd product-service && mvn test
cd order-service && mvn test
cd inventory-service && mvn test
```

### Integration Testing

Test the complete flow:

1. Start all services
2. Create test data using the sample API calls above
3. Verify data consistency across services

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Ensure no other applications are using ports 8080-8084
2. **Service Communication Errors**: Start User and Product services before Order service
3. **Database Connection Issues**: H2 databases are recreated on each restart

### Logs

Check application logs for debugging:

```bash
# Service logs show in the terminal where mvn spring-boot:run was executed
# Look for ERROR or WARN level messages
```

## Development

### Project Structure

```
spring-microservices-sample/
├── README.md
├── user-service/          # User management service
├── product-service/       # Product catalog service
├── order-service/         # Order processing service
├── inventory-service/     # Inventory management service
└── api-gateway/          # API Gateway and routing
```

### Adding New Features

1. Each service is independent - modify only the relevant service
2. Update API contracts carefully to maintain compatibility
3. Add integration tests for new inter-service communication
4. Update this README with new endpoints

## DevOps Ready

This application is designed for DevOps practice and can be extended with:

- **Containerization**: Docker and Docker Compose
- **Orchestration**: Kubernetes manifests
- **CI/CD**: Jenkins, GitHub Actions, or GitLab CI
- **Monitoring**: Prometheus, Grafana, ELK stack
- **Service Mesh**: Istio or Linkerd
- **Configuration Management**: Spring Cloud Config
- **Service Discovery**: Eureka or Consul

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes to individual services
4. Test thoroughly including inter-service communication
5. Submit a pull request

## License

This project is designed for educational and practice purposes.