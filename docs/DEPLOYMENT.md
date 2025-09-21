# Deployment Guide

This guide covers various deployment strategies for the Spring Boot Microservices Sample application.

## Local Development Deployment

### Prerequisites
- Java 17+
- Maven 3.6+
- Git

### Manual Deployment
1. Clone and build each service:
```bash
git clone <repository-url>
cd spring-boot-microservices-sample

# Build all services
mvn clean install
```

2. Start services in order:
```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2 - User Service
cd user-service
mvn spring-boot:run

# Terminal 3 - Product Service
cd product-service
mvn spring-boot:run

# Terminal 4 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 5 - Order Service
cd order-service
mvn spring-boot:run
```

### Using Scripts
Create startup scripts for easier development:

**start-all.sh** (Linux/Mac):
```bash
#!/bin/bash
cd api-gateway && mvn spring-boot:run &
cd user-service && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd inventory-service && mvn spring-boot:run &
cd order-service && mvn spring-boot:run &
wait
```

**start-all.bat** (Windows):
```batch
@echo off
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
start "User Service" cmd /k "cd user-service && mvn spring-boot:run"
start "Product Service" cmd /k "cd product-service && mvn spring-boot:run"
start "Inventory Service" cmd /k "cd inventory-service && mvn spring-boot:run"
start "Order Service" cmd /k "cd order-service && mvn spring-boot:run"
```

## Docker Deployment

### Building Docker Images

Build all services:
```bash
# Build individual services
docker build -t api-gateway:latest ./api-gateway
docker build -t user-service:latest ./user-service
docker build -t product-service:latest ./product-service
docker build -t inventory-service:latest ./inventory-service
docker build -t order-service:latest ./order-service
```

Or use a build script:
```bash
#!/bin/bash
services=("api-gateway" "user-service" "product-service" "inventory-service" "order-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    docker build -t $service:latest ./$service
done
```

### Running with Docker

Run services individually:
```bash
# Create a network for service communication
docker network create microservices-network

# Run services
docker run -d --name user-service --network microservices-network -p 8084:8084 user-service:latest
docker run -d --name product-service --network microservices-network -p 8083:8083 product-service:latest
docker run -d --name inventory-service --network microservices-network -p 8081:8081 inventory-service:latest
docker run -d --name order-service --network microservices-network -p 8082:8082 order-service:latest
docker run -d --name api-gateway --network microservices-network -p 8080:8080 api-gateway:latest
```

### Docker Compose Deployment

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - user-service
      - product-service
      - inventory-service
      - order-service
    networks:
      - microservices-network

  user-service:
    build: ./user-service
    ports:
      - "8084:8084"
    networks:
      - microservices-network

  product-service:
    build: ./product-service
    ports:
      - "8083:8083"
    networks:
      - microservices-network

  inventory-service:
    build: ./inventory-service
    ports:
      - "8081:8081"
    networks:
      - microservices-network

  order-service:
    build: ./order-service
    ports:
      - "8082:8082"
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge
```

Deploy with Docker Compose:
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## Production Deployment

### Kubernetes Deployment

#### Prerequisites
- Kubernetes cluster
- kubectl configured
- Docker images pushed to registry

#### Deployment Manifests

**namespace.yaml**:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: microservices
```

**user-service-deployment.yaml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: microservices
spec:
  replicas: 2
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:latest
        ports:
        - containerPort: 8084
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: microservices
spec:
  selector:
    app: user-service
  ports:
  - port: 8084
    targetPort: 8084
  type: ClusterIP
```

#### Deploy to Kubernetes:
```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy services
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n microservices
kubectl get services -n microservices
```

### Cloud Deployment

#### AWS ECS
1. Push images to ECR
2. Create ECS cluster
3. Define task definitions
4. Create services with load balancers

#### Google Cloud Run
```bash
# Build and push to GCR
gcloud builds submit --tag gcr.io/PROJECT_ID/user-service ./user-service

# Deploy to Cloud Run
gcloud run deploy user-service \
  --image gcr.io/PROJECT_ID/user-service \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

#### Azure Container Instances
```bash
# Create resource group
az group create --name microservices-rg --location eastus

# Deploy container
az container create \
  --resource-group microservices-rg \
  --name user-service \
  --image user-service:latest \
  --ports 8084
```

## Environment Configuration

### Development
```properties
# application-dev.properties
server.port=8084
spring.datasource.url=jdbc:h2:mem:devdb
logging.level.com.example=DEBUG
```

### Production
```properties
# application-prod.properties
server.port=8084
spring.datasource.url=jdbc:postgresql://db:5432/userdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
logging.level.com.example=INFO
```

### Environment Variables
```bash
# Database configuration
DB_USERNAME=admin
DB_PASSWORD=secret123
DB_HOST=localhost
DB_PORT=5432

# Service URLs
USER_SERVICE_URL=http://user-service:8084
PRODUCT_SERVICE_URL=http://product-service:8083
INVENTORY_SERVICE_URL=http://inventory-service:8081
ORDER_SERVICE_URL=http://order-service:8082
```

## Monitoring and Health Checks

### Health Check Endpoints
Each service exposes health check endpoints:
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Service metrics

### Load Balancer Health Checks
Configure load balancers to use health endpoints:
```yaml
# Kubernetes liveness probe
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8084
  initialDelaySeconds: 30
  periodSeconds: 10

# Kubernetes readiness probe
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8084
  initialDelaySeconds: 5
  periodSeconds: 5
```

## Scaling Strategies

### Horizontal Scaling
```bash
# Kubernetes horizontal pod autoscaler
kubectl autoscale deployment user-service --cpu-percent=70 --min=2 --max=10 -n microservices

# Docker Compose scaling
docker-compose up --scale user-service=3
```

### Vertical Scaling
Adjust resource limits in deployment configurations based on monitoring data.

## Troubleshooting

### Common Issues
1. **Port conflicts**: Ensure no other services are using the same ports
2. **Service discovery**: Verify service URLs and network connectivity
3. **Database connections**: Check database availability and credentials
4. **Memory issues**: Monitor and adjust JVM heap settings

### Debugging Commands
```bash
# Check service logs
docker logs user-service
kubectl logs -f deployment/user-service -n microservices

# Check service connectivity
curl http://localhost:8084/actuator/health
kubectl port-forward service/user-service 8084:8084 -n microservices

# Monitor resource usage
docker stats
kubectl top pods -n microservices
```