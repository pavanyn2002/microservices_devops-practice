# DevOps Deployment Guide

Complete guide for deploying Spring Boot Microservices to Azure Cloud and On-Premises Kubernetes with full DevOps toolchain coverage.

## 🎯 Overview

This guide covers end-to-end deployment and monitoring of the Spring Boot microservices application using:

- **Cloud**: Azure Kubernetes Service (AKS)
- **On-Premises**: Kubernetes cluster
- **Infrastructure**: Terraform
- **Containerization**: Docker
- **CI/CD**: GitHub Actions
- **Configuration Management**: Ansible
- **Monitoring**: Prometheus + Grafana
- **Operating System**: Linux (Ubuntu)

## 📋 Prerequisites

### Required Tools
- Docker Desktop
- kubectl
- Terraform
- Ansible
- Azure CLI
- GitHub account
- Linux environment (Ubuntu 20.04+ recommended)

### Required Accounts
- Azure subscription
- GitHub account
- Docker Hub account (optional)

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Repository                         │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Source Code   │    │ GitHub Actions  │                │
│  │                 │    │     CI/CD       │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Docker Registry                          │
│              (Docker Hub / Azure ACR)                      │
└─────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Azure AKS     │  │  On-Prem K8s    │  │   Local Dev     │
│                 │  │                 │  │                 │
│ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌─────────────┐ │
│ │Microservices│ │  │ │Microservices│ │  │ │Microservices│ │
│ └─────────────┘ │  │ └─────────────┘ │  │ └─────────────┘ │
│ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌─────────────┐ │
│ │ Prometheus  │ │  │ │ Prometheus  │ │  │ │   Docker    │ │
│ │  Grafana    │ │  │ │  Grafana    │ │  │  Compose     │ │
│ └─────────────┘ │  │ └─────────────┘ │  │ └─────────────┘ │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## 📚 Table of Contents

1. [Environment Setup](#environment-setup)
2. [Docker Containerization](#docker-containerization)
3. [Local Development with Docker Compose](#local-development)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Azure Cloud Deployment](#azure-cloud-deployment)
6. [On-Premises Kubernetes](#on-premises-kubernetes)
7. [Infrastructure as Code with Terraform](#terraform)
8. [Configuration Management with Ansible](#ansible)
9. [CI/CD with GitHub Actions](#cicd)
10. [Monitoring with Prometheus & Grafana](#monitoring)
11. [Security & Best Practices](#security)
12. [Troubleshooting](#troubleshooting)

---

*This guide is structured to provide hands-on experience with all major DevOps tools and practices.*## 🛠
️ Environment Setup

### 1. Linux Environment Setup (Ubuntu 20.04+)

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install essential tools
sudo apt install -y curl wget git vim unzip software-properties-common

# Install Java 17
sudo apt install -y openjdk-17-jdk
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc

# Install Maven
sudo apt install -y maven

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install Terraform
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install -y terraform

# Install Ansible
sudo apt install -y ansible

# Install Azure CLI
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Install Helm
curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
sudo apt update && sudo apt install -y helm
```

### 2. Verify Installation

```bash
# Verify all tools
java --version
mvn --version
docker --version
docker-compose --version
kubectl version --client
terraform --version
ansible --version
az --version
helm version
```

## 🐳 Docker Containerization

### 1. Create Dockerfiles for Each Service

#### User Service Dockerfile
```dockerfile
# user-service/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/users/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Product Service Dockerfile
```dockerfile
# product-service/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/api/products/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Order Service Dockerfile
```dockerfile
# order-service/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/api/orders/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Inventory Service Dockerfile
```dockerfile
# inventory-service/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/api/inventory/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### API Gateway Dockerfile
```dockerfile
# api-gateway/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build Docker Images

```bash
# Build all images
docker build -t microservices/user-service:latest ./user-service
docker build -t microservices/product-service:latest ./product-service
docker build -t microservices/order-service:latest ./order-service
docker build -t microservices/inventory-service:latest ./inventory-service
docker build -t microservices/api-gateway:latest ./api-gateway

# Verify images
docker images | grep microservices
```

### 3. Multi-stage Dockerfile (Optimized)

```dockerfile
# Example optimized Dockerfile for user-service
FROM maven:3.8.6-openjdk-17-slim AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/users/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```#
# 🐳 Local Development with Docker Compose

### 1. Create Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'

services:
  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/api/users/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - microservices-network

  product-service:
    build: ./product-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/api/products/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - microservices-network

  inventory-service:
    build: ./inventory-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8084/api/inventory/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - microservices-network

  order-service:
    build: ./order-service
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - USER_SERVICE_URL=http://user-service:8081
      - PRODUCT_SERVICE_URL=http://product-service:8082
    depends_on:
      user-service:
        condition: service_healthy
      product-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/api/orders/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - microservices-network

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - USER_SERVICE_URL=http://user-service:8081
      - PRODUCT_SERVICE_URL=http://product-service:8082
      - ORDER_SERVICE_URL=http://order-service:8083
      - INVENTORY_SERVICE_URL=http://inventory-service:8084
    depends_on:
      user-service:
        condition: service_healthy
      product-service:
        condition: service_healthy
      order-service:
        condition: service_healthy
      inventory-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - microservices-network

  # Monitoring Stack
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
    networks:
      - microservices-network

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge

volumes:
  prometheus_data:
  grafana_data:
```

### 2. Create Docker Profile Configuration

Add Docker-specific configuration for each service:

```yaml
# user-service/src/main/resources/application-docker.yml
server:
  port: 8081

spring:
  application:
    name: user-service
  
  datasource:
    url: jdbc:h2:mem:userdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    database-platform: org.hibernate.dialect.H2Dialect

logging:
  level:
    com.example.userservice: INFO
    org.springframework.web: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 3. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps

# Test the application
curl http://localhost:8080/health

# Stop all services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### 4. Development Workflow

```bash
# Rebuild specific service
docker-compose build user-service
docker-compose up -d user-service

# Scale services
docker-compose up -d --scale user-service=2

# View service logs
docker-compose logs -f user-service

# Execute commands in running container
docker-compose exec user-service bash
```

## ☸️ Kubernetes Deployment

### 1. Create Kubernetes Manifests

#### Namespace
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: microservices
  labels:
    name: microservices
```

#### ConfigMap
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: microservices-config
  namespace: microservices
data:
  USER_SERVICE_URL: "http://user-service:8081"
  PRODUCT_SERVICE_URL: "http://product-service:8082"
  ORDER_SERVICE_URL: "http://order-service:8083"
  INVENTORY_SERVICE_URL: "http://inventory-service:8084"
```

#### User Service Deployment
```yaml
# k8s/user-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: microservices
  labels:
    app: user-service
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
        image: microservices/user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/users/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/users/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: microservices
  labels:
    app: user-service
spec:
  selector:
    app: user-service
  ports:
  - port: 8081
    targetPort: 8081
    protocol: TCP
  type: ClusterIP
```

#### API Gateway with LoadBalancer
```yaml
# k8s/api-gateway.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: microservices
  labels:
    app: api-gateway
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: microservices/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        envFrom:
        - configMapRef:
            name: microservices-config
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: microservices
  labels:
    app: api-gateway
spec:
  selector:
    app: api-gateway
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  type: LoadBalancer
```

### 2. Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n microservices
kubectl get services -n microservices

# View logs
kubectl logs -f deployment/user-service -n microservices

# Port forward for testing
kubectl port-forward service/api-gateway 8080:80 -n microservices

# Scale deployment
kubectl scale deployment user-service --replicas=3 -n microservices

# Update deployment
kubectl set image deployment/user-service user-service=microservices/user-service:v2 -n microservices
```## ☁️
 Azure Cloud Deployment

### 1. Azure Setup and Authentication

```bash
# Login to Azure
az login

# Set subscription
az account set --subscription "your-subscription-id"

# Create resource group
az group create --name microservices-rg --location eastus

# Create Azure Container Registry (ACR)
az acr create --resource-group microservices-rg \
  --name microservicesacr --sku Basic

# Login to ACR
az acr login --name microservicesacr
```

### 2. Push Images to Azure Container Registry

```bash
# Tag images for ACR
docker tag microservices/user-service:latest microservicesacr.azurecr.io/user-service:latest
docker tag microservices/product-service:latest microservicesacr.azurecr.io/product-service:latest
docker tag microservices/order-service:latest microservicesacr.azurecr.io/order-service:latest
docker tag microservices/inventory-service:latest microservicesacr.azurecr.io/inventory-service:latest
docker tag microservices/api-gateway:latest microservicesacr.azurecr.io/api-gateway:latest

# Push images to ACR
docker push microservicesacr.azurecr.io/user-service:latest
docker push microservicesacr.azurecr.io/product-service:latest
docker push microservicesacr.azurecr.io/order-service:latest
docker push microservicesacr.azurecr.io/inventory-service:latest
docker push microservicesacr.azurecr.io/api-gateway:latest

# Verify images in ACR
az acr repository list --name microservicesacr --output table
```

### 3. Create Azure Kubernetes Service (AKS)

```bash
# Create AKS cluster
az aks create \
  --resource-group microservices-rg \
  --name microservices-aks \
  --node-count 3 \
  --node-vm-size Standard_B2s \
  --enable-addons monitoring \
  --attach-acr microservicesacr \
  --generate-ssh-keys

# Get AKS credentials
az aks get-credentials --resource-group microservices-rg --name microservices-aks

# Verify connection
kubectl get nodes
```

### 4. Deploy to AKS

Update Kubernetes manifests to use ACR images:

```yaml
# k8s/user-service-aks.yaml
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
        image: microservicesacr.azurecr.io/user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "azure"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/users/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/users/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
```

```bash
# Deploy to AKS
kubectl apply -f k8s/

# Check deployment
kubectl get pods -n microservices
kubectl get services -n microservices

# Get external IP
kubectl get service api-gateway -n microservices
```

### 5. Azure Application Gateway Integration

```bash
# Enable Application Gateway Ingress Controller
az aks enable-addons \
  --resource-group microservices-rg \
  --name microservices-aks \
  --addons ingress-appgw \
  --appgw-name microservices-appgw \
  --appgw-subnet-cidr "10.2.0.0/16"
```

Create Ingress resource:

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: microservices-ingress
  namespace: microservices
  annotations:
    kubernetes.io/ingress.class: azure/application-gateway
    appgw.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 80
```

### 6. Azure Monitor Integration

```yaml
# k8s/azure-monitor.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: container-azm-ms-agentconfig
  namespace: kube-system
data:
  schema-version: v1
  config-version: ver1
  log-data-collection-settings: |-
    [log_collection_settings]
       [log_collection_settings.stdout]
          enabled = true
          exclude_namespaces = ["kube-system"]
       [log_collection_settings.stderr]
          enabled = true
          exclude_namespaces = ["kube-system"]
  prometheus-data-collection-settings: |-
    [prometheus_data_collection_settings.cluster]
        interval = "1m"
        monitor_kubernetes_pods = true
    [prometheus_data_collection_settings.node]
        interval = "1m"
```

## 🏠 On-Premises Kubernetes Setup

### 1. Install Kubernetes with kubeadm

```bash
# Install Docker (if not already installed)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install kubeadm, kubelet, kubectl
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl
sudo curl -fsSLo /usr/share/keyrings/kubernetes-archive-keyring.gpg https://packages.cloud.google.com/apt/doc/apt-key.gpg
echo "deb [signed-by=/usr/share/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# Disable swap
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# Initialize cluster (Master node)
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# Configure kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Install Flannel CNI
kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

# Allow scheduling on master (for single-node cluster)
kubectl taint nodes --all node-role.kubernetes.io/master-
```

### 2. Set up Private Docker Registry

```bash
# Create registry directory
sudo mkdir -p /opt/docker-registry

# Run private registry
docker run -d \
  -p 5000:5000 \
  --restart=always \
  --name registry \
  -v /opt/docker-registry:/var/lib/registry \
  registry:2

# Configure Docker to use insecure registry
echo '{"insecure-registries":["localhost:5000"]}' | sudo tee /etc/docker/daemon.json
sudo systemctl restart docker

# Tag and push images to private registry
docker tag microservices/user-service:latest localhost:5000/user-service:latest
docker push localhost:5000/user-service:latest
```

### 3. Deploy to On-Premises Kubernetes

Update manifests for private registry:

```yaml
# k8s/user-service-onprem.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: microservices
spec:
  replicas: 1
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
        image: localhost:5000/user-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "onprem"
```

### 4. Install MetalLB for LoadBalancer

```bash
# Install MetalLB
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.7/config/manifests/metallb-native.yaml

# Configure IP pool
cat <<EOF | kubectl apply -f -
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: first-pool
  namespace: metallb-system
spec:
  addresses:
  - 192.168.1.240-192.168.1.250
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: example
  namespace: metallb-system
spec:
  ipAddressPools:
  - first-pool
EOF
```

## 🏗️ Infrastructure as Code with Terraform

### 1. Azure Infrastructure with Terraform

Create Terraform configuration:

```hcl
# terraform/azure/main.tf
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location

  tags = {
    Environment = var.environment
    Project     = "microservices"
  }
}

# Container Registry
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true

  tags = {
    Environment = var.environment
  }
}

# AKS Cluster
resource "azurerm_kubernetes_cluster" "aks" {
  name                = var.aks_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "${var.aks_name}-dns"

  default_node_pool {
    name       = "default"
    node_count = var.node_count
    vm_size    = var.node_vm_size
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = var.environment
  }
}

# Attach ACR to AKS
resource "azurerm_role_assignment" "aks_acr" {
  principal_id                     = azurerm_kubernetes_cluster.aks.kubelet_identity[0].object_id
  role_definition_name             = "AcrPull"
  scope                           = azurerm_container_registry.acr.id
  skip_service_principal_aad_check = true
}

# Log Analytics Workspace
resource "azurerm_log_analytics_workspace" "main" {
  name                = "${var.aks_name}-logs"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = 30

  tags = {
    Environment = var.environment
  }
}
```

```hcl
# terraform/azure/variables.tf
variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
  default     = "microservices-rg"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "acr_name" {
  description = "Name of the Azure Container Registry"
  type        = string
  default     = "microservicesacr"
}

variable "aks_name" {
  description = "Name of the AKS cluster"
  type        = string
  default     = "microservices-aks"
}

variable "node_count" {
  description = "Number of nodes in the AKS cluster"
  type        = number
  default     = 3
}

variable "node_vm_size" {
  description = "VM size for AKS nodes"
  type        = string
  default     = "Standard_B2s"
}
```

```hcl
# terraform/azure/outputs.tf
output "resource_group_name" {
  value = azurerm_resource_group.main.name
}

output "aks_cluster_name" {
  value = azurerm_kubernetes_cluster.aks.name
}

output "acr_login_server" {
  value = azurerm_container_registry.acr.login_server
}

output "aks_cluster_id" {
  value = azurerm_kubernetes_cluster.aks.id
}
```

### 2. Deploy with Terraform

```bash
# Initialize Terraform
cd terraform/azure
terraform init

# Plan deployment
terraform plan

# Apply configuration
terraform apply

# Get AKS credentials
az aks get-credentials --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw aks_cluster_name)

# Destroy infrastructure (when done)
terraform destroy
```## 🔧
 Configuration Management with Ansible

### 1. Ansible Inventory and Configuration

```ini
# ansible/inventory/hosts
[kubernetes_masters]
k8s-master ansible_host=192.168.1.100 ansible_user=ubuntu

[kubernetes_workers]
k8s-worker1 ansible_host=192.168.1.101 ansible_user=ubuntu
k8s-worker2 ansible_host=192.168.1.102 ansible_user=ubuntu

[kubernetes:children]
kubernetes_masters
kubernetes_workers

[all:vars]
ansible_ssh_private_key_file=~/.ssh/id_rsa
ansible_ssh_common_args='-o StrictHostKeyChecking=no'
```

```yaml
# ansible/ansible.cfg
[defaults]
inventory = inventory/hosts
host_key_checking = False
remote_user = ubuntu
private_key_file = ~/.ssh/id_rsa
timeout = 30

[ssh_connection]
ssh_args = -o ControlMaster=auto -o ControlPersist=60s
pipelining = True
```

### 2. Kubernetes Cluster Setup Playbook

```yaml
# ansible/playbooks/k8s-cluster-setup.yml
---
- name: Setup Kubernetes Cluster
  hosts: kubernetes
  become: yes
  vars:
    kubernetes_version: "1.28.0"
    pod_network_cidr: "10.244.0.0/16"
  
  tasks:
    - name: Update system packages
      apt:
        update_cache: yes
        upgrade: dist

    - name: Install required packages
      apt:
        name:
          - apt-transport-https
          - ca-certificates
          - curl
          - gnupg
          - lsb-release
        state: present

    - name: Add Docker GPG key
      apt_key:
        url: https://download.docker.com/linux/ubuntu/gpg
        state: present

    - name: Add Docker repository
      apt_repository:
        repo: "deb [arch=amd64] https://download.docker.com/linux/ubuntu {{ ansible_distribution_release }} stable"
        state: present

    - name: Install Docker
      apt:
        name: docker-ce
        state: present

    - name: Add user to docker group
      user:
        name: "{{ ansible_user }}"
        groups: docker
        append: yes

    - name: Add Kubernetes GPG key
      apt_key:
        url: https://packages.cloud.google.com/apt/doc/apt-key.gpg
        state: present

    - name: Add Kubernetes repository
      apt_repository:
        repo: "deb https://apt.kubernetes.io/ kubernetes-xenial main"
        state: present

    - name: Install Kubernetes components
      apt:
        name:
          - kubelet={{ kubernetes_version }}-00
          - kubeadm={{ kubernetes_version }}-00
          - kubectl={{ kubernetes_version }}-00
        state: present

    - name: Hold Kubernetes packages
      dpkg_selections:
        name: "{{ item }}"
        selection: hold
      loop:
        - kubelet
        - kubeadm
        - kubectl

    - name: Disable swap
      command: swapoff -a
      when: ansible_swaptotal_mb > 0

    - name: Remove swap from fstab
      lineinfile:
        path: /etc/fstab
        regexp: '^.*swap.*$'
        state: absent

- name: Initialize Kubernetes Master
  hosts: kubernetes_masters
  become: yes
  tasks:
    - name: Initialize Kubernetes cluster
      command: kubeadm init --pod-network-cidr={{ pod_network_cidr }}
      register: kubeadm_init
      changed_when: kubeadm_init.rc == 0

    - name: Create .kube directory
      file:
        path: /home/{{ ansible_user }}/.kube
        state: directory
        owner: "{{ ansible_user }}"
        group: "{{ ansible_user }}"

    - name: Copy admin.conf to user's kube config
      copy:
        src: /etc/kubernetes/admin.conf
        dest: /home/{{ ansible_user }}/.kube/config
        remote_src: yes
        owner: "{{ ansible_user }}"
        group: "{{ ansible_user }}"

    - name: Install Flannel CNI
      become_user: "{{ ansible_user }}"
      command: kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

    - name: Get join command
      command: kubeadm token create --print-join-command
      register: join_command

    - name: Save join command
      set_fact:
        kubernetes_join_command: "{{ join_command.stdout }}"

- name: Join worker nodes
  hosts: kubernetes_workers
  become: yes
  tasks:
    - name: Join cluster
      command: "{{ hostvars[groups['kubernetes_masters'][0]]['kubernetes_join_command'] }}"
      register: join_result
      changed_when: join_result.rc == 0
```

### 3. Application Deployment Playbook

```yaml
# ansible/playbooks/deploy-microservices.yml
---
- name: Deploy Microservices to Kubernetes
  hosts: kubernetes_masters
  become_user: "{{ ansible_user }}"
  vars:
    namespace: microservices
    image_tag: latest
    registry_url: localhost:5000
  
  tasks:
    - name: Create namespace
      kubernetes.core.k8s:
        name: "{{ namespace }}"
        api_version: v1
        kind: Namespace
        state: present

    - name: Deploy ConfigMap
      kubernetes.core.k8s:
        definition:
          apiVersion: v1
          kind: ConfigMap
          metadata:
            name: microservices-config
            namespace: "{{ namespace }}"
          data:
            USER_SERVICE_URL: "http://user-service:8081"
            PRODUCT_SERVICE_URL: "http://product-service:8082"
            ORDER_SERVICE_URL: "http://order-service:8083"
            INVENTORY_SERVICE_URL: "http://inventory-service:8084"

    - name: Deploy User Service
      kubernetes.core.k8s:
        definition:
          apiVersion: apps/v1
          kind: Deployment
          metadata:
            name: user-service
            namespace: "{{ namespace }}"
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
                  image: "{{ registry_url }}/user-service:{{ image_tag }}"
                  ports:
                  - containerPort: 8081
                  env:
                  - name: SPRING_PROFILES_ACTIVE
                    value: "kubernetes"

    - name: Create User Service
      kubernetes.core.k8s:
        definition:
          apiVersion: v1
          kind: Service
          metadata:
            name: user-service
            namespace: "{{ namespace }}"
          spec:
            selector:
              app: user-service
            ports:
            - port: 8081
              targetPort: 8081

    - name: Wait for User Service to be ready
      kubernetes.core.k8s_info:
        api_version: apps/v1
        kind: Deployment
        name: user-service
        namespace: "{{ namespace }}"
        wait_condition:
          type: Available
          status: "True"
        wait_timeout: 300
```

### 4. Monitoring Setup Playbook

```yaml
# ansible/playbooks/setup-monitoring.yml
---
- name: Setup Prometheus and Grafana
  hosts: kubernetes_masters
  become_user: "{{ ansible_user }}"
  vars:
    monitoring_namespace: monitoring
  
  tasks:
    - name: Create monitoring namespace
      kubernetes.core.k8s:
        name: "{{ monitoring_namespace }}"
        api_version: v1
        kind: Namespace
        state: present

    - name: Add Prometheus Helm repository
      kubernetes.core.helm_repository:
        name: prometheus-community
        repo_url: https://prometheus-community.github.io/helm-charts

    - name: Install Prometheus
      kubernetes.core.helm:
        name: prometheus
        chart_ref: prometheus-community/kube-prometheus-stack
        release_namespace: "{{ monitoring_namespace }}"
        create_namespace: true
        values:
          grafana:
            adminPassword: admin123
            service:
              type: NodePort
              nodePort: 30000
          prometheus:
            service:
              type: NodePort
              nodePort: 30001

    - name: Wait for Prometheus to be ready
      kubernetes.core.k8s_info:
        api_version: apps/v1
        kind: Deployment
        name: prometheus-kube-prometheus-prometheus-operator
        namespace: "{{ monitoring_namespace }}"
        wait_condition:
          type: Available
          status: "True"
        wait_timeout: 600
```

### 5. Run Ansible Playbooks

```bash
# Install Ansible collections
ansible-galaxy collection install kubernetes.core

# Setup Kubernetes cluster
ansible-playbook -i inventory/hosts playbooks/k8s-cluster-setup.yml

# Deploy microservices
ansible-playbook -i inventory/hosts playbooks/deploy-microservices.yml

# Setup monitoring
ansible-playbook -i inventory/hosts playbooks/setup-monitoring.yml

# Check deployment status
ansible kubernetes_masters -i inventory/hosts -m shell -a "kubectl get pods -A"
```

## 🚀 CI/CD with GitHub Actions

### 1. GitHub Actions Workflow

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests for User Service
      run: |
        cd user-service
        mvn clean test
    
    - name: Run tests for Product Service
      run: |
        cd product-service
        mvn clean test
    
    - name: Run tests for Order Service
      run: |
        cd order-service
        mvn clean test
    
    - name: Run tests for Inventory Service
      run: |
        cd inventory-service
        mvn clean test
    
    - name: Run tests for API Gateway
      run: |
        cd api-gateway
        mvn clean test

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    strategy:
      matrix:
        service: [user-service, product-service, order-service, inventory-service, api-gateway]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/${{ matrix.service }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: ./${{ matrix.service }}
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

  deploy-to-staging:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: staging
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'v1.28.0'
    
    - name: Configure kubectl for AKS
      run: |
        echo "${{ secrets.KUBE_CONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
        kubectl config current-context
    
    - name: Deploy to staging
      run: |
        export KUBECONFIG=kubeconfig
        kubectl set image deployment/user-service user-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/user-service:latest -n microservices-staging
        kubectl set image deployment/product-service product-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/product-service:latest -n microservices-staging
        kubectl set image deployment/order-service order-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/order-service:latest -n microservices-staging
        kubectl set image deployment/inventory-service inventory-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/inventory-service:latest -n microservices-staging
        kubectl set image deployment/api-gateway api-gateway=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/api-gateway:latest -n microservices-staging
    
    - name: Verify deployment
      run: |
        export KUBECONFIG=kubeconfig
        kubectl rollout status deployment/user-service -n microservices-staging
        kubectl rollout status deployment/product-service -n microservices-staging
        kubectl rollout status deployment/order-service -n microservices-staging
        kubectl rollout status deployment/inventory-service -n microservices-staging
        kubectl rollout status deployment/api-gateway -n microservices-staging

  deploy-to-production:
    needs: deploy-to-staging
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'v1.28.0'
    
    - name: Configure kubectl for AKS
      run: |
        echo "${{ secrets.KUBE_CONFIG_PROD }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
        kubectl config current-context
    
    - name: Deploy to production
      run: |
        export KUBECONFIG=kubeconfig
        kubectl set image deployment/user-service user-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/user-service:latest -n microservices
        kubectl set image deployment/product-service product-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/product-service:latest -n microservices
        kubectl set image deployment/order-service order-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/order-service:latest -n microservices
        kubectl set image deployment/inventory-service inventory-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/inventory-service:latest -n microservices
        kubectl set image deployment/api-gateway api-gateway=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/api-gateway:latest -n microservices
    
    - name: Verify production deployment
      run: |
        export KUBECONFIG=kubeconfig
        kubectl rollout status deployment/user-service -n microservices
        kubectl rollout status deployment/product-service -n microservices
        kubectl rollout status deployment/order-service -n microservices
        kubectl rollout status deployment/inventory-service -n microservices
        kubectl rollout status deployment/api-gateway -n microservices
```

### 2. Security Scanning Workflow

```yaml
# .github/workflows/security.yml
name: Security Scanning

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * 1'  # Weekly on Monday at 2 AM

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run OWASP Dependency Check
      run: |
        for service in user-service product-service order-service inventory-service api-gateway; do
          cd $service
          mvn org.owasp:dependency-check-maven:check
          cd ..
        done
    
    - name: Upload dependency check results
      uses: actions/upload-artifact@v3
      with:
        name: dependency-check-report
        path: '**/target/dependency-check-report.html'

  container-scan:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        service: [user-service, product-service, order-service, inventory-service, api-gateway]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Build Docker image
      run: |
        docker build -t ${{ matrix.service }}:latest ./${{ matrix.service }}
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: '${{ matrix.service }}:latest'
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy scan results
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
```## 
📊 Monitoring with Prometheus & Grafana

### 1. Prometheus Configuration

```yaml
# monitoring/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'product-service'
    static_configs:
      - targets: ['product-service:8082']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'order-service'
    static_configs:
      - targets: ['order-service:8083']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'inventory-service'
    static_configs:
      - targets: ['inventory-service:8084']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'kubernetes-apiservers'
    kubernetes_sd_configs:
    - role: endpoints
    scheme: https
    tls_config:
      ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
    bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
    relabel_configs:
    - source_labels: [__meta_kubernetes_namespace, __meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
      action: keep
      regex: default;kubernetes;https

  - job_name: 'kubernetes-nodes'
    kubernetes_sd_configs:
    - role: node
    scheme: https
    tls_config:
      ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
    bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
    relabel_configs:
    - action: labelmap
      regex: __meta_kubernetes_node_label_(.+)

  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
    - role: pod
    relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: true
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
      action: replace
      target_label: __metrics_path__
      regex: (.+)
    - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
      action: replace
      regex: ([^:]+)(?::\d+)?;(\d+)
      replacement: $1:$2
      target_label: __address__
    - action: labelmap
      regex: __meta_kubernetes_pod_label_(.+)
    - source_labels: [__meta_kubernetes_namespace]
      action: replace
      target_label: kubernetes_namespace
    - source_labels: [__meta_kubernetes_pod_name]
      action: replace
      target_label: kubernetes_pod_name
```

### 2. Alert Rules

```yaml
# monitoring/alert_rules.yml
groups:
- name: microservices.rules
  rules:
  - alert: ServiceDown
    expr: up == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Service {{ $labels.instance }} is down"
      description: "{{ $labels.instance }} of job {{ $labels.job }} has been down for more than 1 minute."

  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High error rate on {{ $labels.instance }}"
      description: "Error rate is {{ $value }} errors per second on {{ $labels.instance }}"

  - alert: HighMemoryUsage
    expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes > 0.8
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High memory usage on {{ $labels.instance }}"
      description: "Memory usage is above 80% on {{ $labels.instance }}"

  - alert: HighCPUUsage
    expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High CPU usage on {{ $labels.instance }}"
      description: "CPU usage is above 80% on {{ $labels.instance }}"

  - alert: PodCrashLooping
    expr: rate(kube_pod_container_status_restarts_total[15m]) > 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Pod {{ $labels.pod }} is crash looping"
      description: "Pod {{ $labels.pod }} in namespace {{ $labels.namespace }} is restarting frequently"
```

### 3. Grafana Dashboard Configuration

```json
# monitoring/grafana/dashboards/microservices-dashboard.json
{
  "dashboard": {
    "id": null,
    "title": "Microservices Dashboard",
    "tags": ["microservices"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Service Health",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=~\".*-service|api-gateway\"}",
            "legendFormat": "{{ job }}"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "thresholds"
            },
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "green", "value": 1}
              ]
            }
          }
        },
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{ job }} - {{ method }} {{ status }}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "{{ job }} - 95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "{{ job }} - 50th percentile"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=~\".*-service|api-gateway\"}",
            "legendFormat": "{{ job }} - {{ area }}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 16}
      },
      {
        "id": 5,
        "title": "CPU Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(process_cpu_seconds_total{job=~\".*-service|api-gateway\"}[5m])",
            "legendFormat": "{{ job }}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 16}
      }
    ],
    "time": {"from": "now-1h", "to": "now"},
    "refresh": "30s"
  }
}
```

### 4. Grafana Datasource Configuration

```yaml
# monitoring/grafana/datasources/prometheus.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
```

### 5. Deploy Monitoring Stack to Kubernetes

```yaml
# k8s/monitoring/prometheus-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      serviceAccountName: prometheus
      containers:
      - name: prometheus
        image: prom/prometheus:latest
        ports:
        - containerPort: 9090
        volumeMounts:
        - name: config-volume
          mountPath: /etc/prometheus
        - name: storage-volume
          mountPath: /prometheus
        command:
        - '/bin/prometheus'
        - '--config.file=/etc/prometheus/prometheus.yml'
        - '--storage.tsdb.path=/prometheus'
        - '--web.console.libraries=/etc/prometheus/console_libraries'
        - '--web.console.templates=/etc/prometheus/consoles'
        - '--web.enable-lifecycle'
      volumes:
      - name: config-volume
        configMap:
          name: prometheus-config
      - name: storage-volume
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: prometheus
  namespace: monitoring
spec:
  selector:
    app: prometheus
  ports:
  - port: 9090
    targetPort: 9090
  type: ClusterIP
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: prometheus
  namespace: monitoring
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: prometheus
rules:
- apiGroups: [""]
  resources:
  - nodes
  - nodes/proxy
  - services
  - endpoints
  - pods
  verbs: ["get", "list", "watch"]
- apiGroups:
  - extensions
  resources:
  - ingresses
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: prometheus
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: prometheus
subjects:
- kind: ServiceAccount
  name: prometheus
  namespace: monitoring
```

### 6. Add Metrics to Spring Boot Services

Update each service's `application.yml`:

```yaml
# Add to each service's application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

Add dependency to each service's `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## 🔒 Security & Best Practices

### 1. Container Security

```dockerfile
# Secure Dockerfile example
FROM openjdk:17-jdk-slim

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy and build application
COPY pom.xml .
COPY src ./src

RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/* ~/.m2 && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/users/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Kubernetes Security Policies

```yaml
# k8s/security/pod-security-policy.yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: microservices-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

### 3. Network Policies

```yaml
# k8s/security/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: microservices-network-policy
  namespace: microservices
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: microservices
    - namespaceSelector:
        matchLabels:
          name: monitoring
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: microservices
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

### 4. Secrets Management

```yaml
# k8s/security/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: microservices-secrets
  namespace: microservices
type: Opaque
data:
  database-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-jwt-secret>
---
apiVersion: v1
kind: Secret
metadata:
  name: registry-secret
  namespace: microservices
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: <base64-encoded-docker-config>
```

## 🔧 Troubleshooting

### 1. Common Issues and Solutions

#### Docker Issues
```bash
# Container won't start
docker logs <container-id>

# Check resource usage
docker stats

# Clean up resources
docker system prune -a

# Fix permission issues
sudo chown -R $USER:$USER ~/.docker
```

#### Kubernetes Issues
```bash
# Pod stuck in Pending
kubectl describe pod <pod-name> -n <namespace>

# Check node resources
kubectl top nodes

# Check events
kubectl get events -n <namespace> --sort-by='.lastTimestamp'

# Debug networking
kubectl exec -it <pod-name> -n <namespace> -- nslookup <service-name>
```

#### Service Communication Issues
```bash
# Test service connectivity
kubectl exec -it <pod-name> -n <namespace> -- curl http://<service-name>:<port>/health

# Check service endpoints
kubectl get endpoints -n <namespace>

# Verify DNS resolution
kubectl exec -it <pod-name> -n <namespace> -- nslookup <service-name>.<namespace>.svc.cluster.local
```

### 2. Monitoring and Debugging

```bash
# Check application logs
kubectl logs -f deployment/<service-name> -n <namespace>

# Monitor resource usage
kubectl top pods -n <namespace>

# Check service mesh (if using Istio)
istioctl proxy-status
istioctl proxy-config cluster <pod-name>.<namespace>

# Debug with temporary pod
kubectl run debug --image=nicolaka/netshoot -it --rm -- /bin/bash
```

### 3. Performance Tuning

```yaml
# JVM tuning for containers
env:
- name: JAVA_OPTS
  value: "-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"
```

## 🎯 Practice Scenarios

### Scenario 1: Complete Local to Cloud Migration
1. Start with local Docker Compose
2. Move to local Kubernetes (minikube/kind)
3. Deploy to Azure AKS
4. Set up monitoring and alerting
5. Implement CI/CD pipeline

### Scenario 2: Disaster Recovery
1. Simulate node failure
2. Test backup and restore procedures
3. Implement multi-region deployment
4. Test failover scenarios

### Scenario 3: Security Hardening
1. Implement Pod Security Standards
2. Set up network policies
3. Add secrets management
4. Implement RBAC
5. Set up security scanning

### Scenario 4: Performance Optimization
1. Load test the application
2. Identify bottlenecks
3. Implement caching
4. Optimize resource allocation
5. Set up auto-scaling

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Azure AKS Documentation](https://docs.microsoft.com/en-us/azure/aks/)
- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Ansible Kubernetes Collection](https://docs.ansible.com/ansible/latest/collections/kubernetes/core/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

This comprehensive guide provides hands-on experience with all major DevOps tools and practices. Start with local development and gradually progress through containerization, orchestration, cloud deployment, and full CI/CD implementation.