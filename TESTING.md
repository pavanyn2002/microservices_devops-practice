# Testing Guide

This document provides comprehensive testing instructions for the Spring Boot Microservices Sample Application.

## Testing Overview

The application includes multiple levels of testing:

1. **Unit Tests** - Individual component testing
2. **Integration Tests** - Service-level testing with databases
3. **End-to-End Tests** - Complete workflow testing
4. **API Tests** - REST endpoint validation
5. **Inter-Service Communication Tests** - Microservices interaction testing

## Prerequisites for Testing

### Required Software
- Java 17+
- Maven 3.6+
- curl (for API testing)
- Optional: Postman (for GUI testing)

### Services Must Be Running
Before running integration tests, ensure all services are started:

```bash
# Start in separate terminals
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run  
cd inventory-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

## Unit Testing

### Run Unit Tests for All Services

```bash
# User Service
cd user-service
mvn test

# Product Service  
cd product-service
mvn test

# Order Service
cd order-service
mvn test

# Inventory Service
cd inventory-service
mvn test

# API Gateway
cd api-gateway
mvn test
```

### Unit Test Coverage

Each service includes tests for:
- **Model Validation** - Entity validation rules
- **Repository Operations** - Database interactions
- **Service Logic** - Business logic with mocked dependencies
- **Controller Endpoints** - REST API endpoints with mocked services

## Integration Testing

### Database Integration Tests

Each service includes `@DataJpaTest` tests that verify:
- Entity persistence
- Repository queries
- Database constraints
- Data relationships

### Service Integration Tests

Run with `@SpringBootTest` to test:
- Complete service functionality
- H2 database integration
- Configuration loading
- Bean wiring

```bash
# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

## End-to-End Testing

### Automated API Testing

Use the provided test scripts to validate complete workflows:

#### Linux/Mac
```bash
./test-api.sh
```

#### Windows
```bash
test-api.bat
```

### Manual Testing with Postman

1. Import `postman-collection.json` into Postman
2. Set environment variables:
   - `gateway_url`: http://localhost:8080
   - `user_service_url`: http://localhost:8081
   - `product_service_url`: http://localhost:8082
   - `order_service_url`: http://localhost:8083
   - `inventory_service_url`: http://localhost:8084

3. Run the collection or individual requests

## Test Scenarios

### Scenario 1: Complete User-to-Order Flow

This tests the entire microservices interaction:

1. **Create User**
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","firstName":"Test","lastName":"User"}'
   ```

2. **Create Product**
   ```bash
   curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Test Product","price":99.99,"category":"Test"}'
   ```

3. **Add Inventory**
   ```bash
   curl -X POST http://localhost:8080/api/inventory \
     -H "Content-Type: application/json" \
     -d '{"productId":1,"availableStock":10}'
   ```

4. **Create Order** (Tests inter-service communication)
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"userId":1,"orderItems":[{"productId":1,"quantity":2}]}'
   ```

5. **Verify Order** (Check that pricing was fetched from Product Service)
   ```bash
   curl http://localhost:8080/api/orders/1
   ```

### Scenario 2: Error Handling

Test error conditions and fallback behavior:

1. **Invalid User in Order**
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"userId":999,"orderItems":[{"productId":1,"quantity":1}]}'
   ```
   Expected: 400 Bad Request with validation error

2. **Invalid Product in Order**
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"userId":1,"orderItems":[{"productId":999,"quantity":1}]}'
   ```
   Expected: 400 Bad Request with validation error

3. **Service Unavailable** (Stop User Service and try to create order)
   Expected: 503 Service Unavailable

### Scenario 3: Inventory Management

Test stock reservation and release:

1. **Check Initial Stock**
   ```bash
   curl http://localhost:8080/api/inventory/1
   ```

2. **Reserve Stock**
   ```bash
   curl -X POST http://localhost:8080/api/inventory/1/reserve \
     -H "Content-Type: application/json" \
     -d '{"quantity":5}'
   ```

3. **Verify Reservation** (Available stock should decrease, reserved should increase)
   ```bash
   curl http://localhost:8080/api/inventory/1
   ```

4. **Release Stock**
   ```bash
   curl -X POST http://localhost:8080/api/inventory/1/release \
     -H "Content-Type: application/json" \
     -d '{"quantity":3}'
   ```

5. **Verify Release**
   ```bash
   curl http://localhost:8080/api/inventory/1
   ```

## Performance Testing

### Simple Load Testing

Test basic performance with concurrent requests:

```bash
# 10 concurrent requests to get all users
for i in {1..10}; do
  curl -s http://localhost:8080/api/users > /dev/null &
done
wait
echo "Completed 10 concurrent requests"
```

### Response Time Testing

Measure API response times:

```bash
# Test response time for order creation
time curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"orderItems":[{"productId":1,"quantity":1}]}'
```

## Database Verification

### H2 Console Access

Verify data persistence through H2 consoles:

- User Service: http://localhost:8081/h2-console
- Product Service: http://localhost:8082/h2-console  
- Order Service: http://localhost:8083/h2-console
- Inventory Service: http://localhost:8084/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:[servicename]db`
- Username: `sa`
- Password: (empty)

### Data Verification Queries

```sql
-- User Service
SELECT * FROM users;

-- Product Service  
SELECT * FROM products;

-- Order Service
SELECT * FROM orders;
SELECT * FROM order_items;

-- Inventory Service
SELECT * FROM inventory_items;
```

## Health Monitoring

### Service Health Checks

```bash
# Individual services
curl http://localhost:8081/api/users/health
curl http://localhost:8082/api/products/health
curl http://localhost:8083/api/orders/health
curl http://localhost:8084/api/inventory/health

# Gateway with all services
curl http://localhost:8080/health
```

### Expected Health Response

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

## Troubleshooting Tests

### Common Test Failures

1. **Connection Refused**
   - Ensure all services are running
   - Check port availability (8080-8084)
   - Verify no firewall blocking

2. **404 Not Found**
   - Check service URLs
   - Verify endpoints are correct
   - Ensure services started successfully

3. **Inter-Service Communication Errors**
   - Start User and Product services before Order service
   - Check service URLs in application.yml
   - Verify network connectivity between services

4. **Database Errors**
   - H2 databases are in-memory and reset on restart
   - Check H2 console for data verification
   - Ensure proper JPA configuration

### Debug Logging

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.web: DEBUG
```

## Test Automation

### CI/CD Integration

For automated testing in CI/CD pipelines:

```bash
# Build and test all services
mvn clean test -f user-service/pom.xml
mvn clean test -f product-service/pom.xml  
mvn clean test -f order-service/pom.xml
mvn clean test -f inventory-service/pom.xml
mvn clean test -f api-gateway/pom.xml

# Package applications
mvn clean package -f user-service/pom.xml
mvn clean package -f product-service/pom.xml
mvn clean package -f order-service/pom.xml  
mvn clean package -f inventory-service/pom.xml
mvn clean package -f api-gateway/pom.xml
```

### Docker Testing (Future Enhancement)

When containerized, test with:

```bash
# Start all services with Docker Compose
docker-compose up -d

# Wait for services to be ready
sleep 30

# Run API tests
./test-api.sh

# Cleanup
docker-compose down
```

## Test Results Validation

### Success Criteria

✅ All unit tests pass  
✅ All integration tests pass  
✅ All services start successfully  
✅ Health checks return "UP" status  
✅ User-to-order flow completes successfully  
✅ Inter-service communication works  
✅ Error handling returns appropriate status codes  
✅ Database persistence verified  
✅ API Gateway routes requests correctly  

### Performance Benchmarks

- API response time < 500ms for simple operations
- Order creation (with validation) < 1000ms
- Concurrent requests handled without errors
- Memory usage stable during testing

## Next Steps

After successful testing:

1. **DevOps Integration** - Add Docker, Kubernetes
2. **Monitoring** - Add Prometheus, Grafana
3. **Security** - Add authentication, authorization
4. **Performance** - Add caching, optimization
5. **Resilience** - Add circuit breakers, retries