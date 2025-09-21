#!/bin/bash

# Spring Boot Microservices API Testing Script
# This script tests all API endpoints to verify the application is working correctly

set -e  # Exit on any error

echo "🚀 Starting Spring Boot Microservices API Tests"
echo "================================================"

# Configuration
GATEWAY_URL="http://localhost:8080"
USER_SERVICE_URL="http://localhost:8081"
PRODUCT_SERVICE_URL="http://localhost:8082"
ORDER_SERVICE_URL="http://localhost:8083"
INVENTORY_SERVICE_URL="http://localhost:8084"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check if service is running
check_service() {
    local service_name=$1
    local url=$2
    
    print_status "Checking $service_name..."
    
    if curl -s -f "$url/health" > /dev/null 2>&1; then
        print_success "$service_name is running"
        return 0
    else
        print_error "$service_name is not running at $url"
        return 1
    fi
}

# Function to make API call and check response
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    print_status "Testing: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url")
    fi
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "✓ $description (Status: $http_code)"
        echo "$body"
        return 0
    else
        print_error "✗ $description (Expected: $expected_status, Got: $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Step 1: Check if all services are running
echo -e "\n${BLUE}Step 1: Service Health Checks${NC}"
echo "================================"

check_service "User Service" "$USER_SERVICE_URL/api/users" || exit 1
check_service "Product Service" "$PRODUCT_SERVICE_URL/api/products" || exit 1
check_service "Order Service" "$ORDER_SERVICE_URL/api/orders" || exit 1
check_service "Inventory Service" "$INVENTORY_SERVICE_URL/api/inventory" || exit 1
check_service "API Gateway" "$GATEWAY_URL" || exit 1

# Step 2: Test API Gateway Health
echo -e "\n${BLUE}Step 2: API Gateway Health Check${NC}"
echo "=================================="

api_call "GET" "$GATEWAY_URL/health" "" 200 "Gateway health check with all services status"

# Step 3: Test User Service via Gateway
echo -e "\n${BLUE}Step 3: User Service Tests${NC}"
echo "============================"

# Create users
USER1_DATA='{"username":"johndoe","email":"john@example.com","firstName":"John","lastName":"Doe"}'
USER2_DATA='{"username":"janedoe","email":"jane@example.com","firstName":"Jane","lastName":"Doe"}'

api_call "POST" "$GATEWAY_URL/api/users" "$USER1_DATA" 201 "Create user 1 (John Doe)"
api_call "POST" "$GATEWAY_URL/api/users" "$USER2_DATA" 201 "Create user 2 (Jane Doe)"

# Get all users
api_call "GET" "$GATEWAY_URL/api/users" "" 200 "Get all users"

# Get specific user
api_call "GET" "$GATEWAY_URL/api/users/1" "" 200 "Get user by ID (1)"

# Step 4: Test Product Service via Gateway
echo -e "\n${BLUE}Step 4: Product Service Tests${NC}"
echo "==============================="

# Create products
PRODUCT1_DATA='{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1299.99,"category":"Electronics"}'
PRODUCT2_DATA='{"name":"Wireless Mouse","description":"Ergonomic wireless mouse","price":29.99,"category":"Electronics"}'
PRODUCT3_DATA='{"name":"Programming Book","description":"Learn Spring Boot","price":49.99,"category":"Books"}'

api_call "POST" "$GATEWAY_URL/api/products" "$PRODUCT1_DATA" 201 "Create product 1 (Gaming Laptop)"
api_call "POST" "$GATEWAY_URL/api/products" "$PRODUCT2_DATA" 201 "Create product 2 (Wireless Mouse)"
api_call "POST" "$GATEWAY_URL/api/products" "$PRODUCT3_DATA" 201 "Create product 3 (Programming Book)"

# Get all products
api_call "GET" "$GATEWAY_URL/api/products" "" 200 "Get all products"

# Get products by category
api_call "GET" "$GATEWAY_URL/api/products/category/Electronics" "" 200 "Get products by category (Electronics)"

# Search products
api_call "GET" "$GATEWAY_URL/api/products/search?name=laptop" "" 200 "Search products by name (laptop)"

# Step 5: Test Inventory Service via Gateway
echo -e "\n${BLUE}Step 5: Inventory Service Tests${NC}"
echo "================================="

# Create inventory items
INVENTORY1_DATA='{"productId":1,"availableStock":50}'
INVENTORY2_DATA='{"productId":2,"availableStock":100}'
INVENTORY3_DATA='{"productId":3,"availableStock":25}'

api_call "POST" "$GATEWAY_URL/api/inventory" "$INVENTORY1_DATA" 201 "Create inventory for product 1"
api_call "POST" "$GATEWAY_URL/api/inventory" "$INVENTORY2_DATA" 201 "Create inventory for product 2"
api_call "POST" "$GATEWAY_URL/api/inventory" "$INVENTORY3_DATA" 201 "Create inventory for product 3"

# Get all inventory
api_call "GET" "$GATEWAY_URL/api/inventory" "" 200 "Get all inventory items"

# Check stock availability
api_call "GET" "$GATEWAY_URL/api/inventory/1/available?quantity=5" "" 200 "Check stock availability for product 1"

# Reserve stock
RESERVE_DATA='{"quantity":5}'
api_call "POST" "$GATEWAY_URL/api/inventory/1/reserve" "$RESERVE_DATA" 200 "Reserve 5 units of product 1"

# Check inventory after reservation
api_call "GET" "$GATEWAY_URL/api/inventory/1" "" 200 "Get inventory for product 1 after reservation"

# Step 6: Test Order Service via Gateway (Inter-service communication)
echo -e "\n${BLUE}Step 6: Order Service Tests (Inter-service Communication)${NC}"
echo "=========================================================="

# Create orders
ORDER1_DATA='{"userId":1,"orderItems":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'
ORDER2_DATA='{"userId":2,"orderItems":[{"productId":3,"quantity":1}]}'

api_call "POST" "$GATEWAY_URL/api/orders" "$ORDER1_DATA" 201 "Create order 1 (John Doe - Laptop + Mouse)"
api_call "POST" "$GATEWAY_URL/api/orders" "$ORDER2_DATA" 201 "Create order 2 (Jane Doe - Book)"

# Get all orders
api_call "GET" "$GATEWAY_URL/api/orders" "" 200 "Get all orders"

# Get orders by user
api_call "GET" "$GATEWAY_URL/api/orders/user/1" "" 200 "Get orders for user 1 (John Doe)"

# Update order status
api_call "PUT" "$GATEWAY_URL/api/orders/1?status=CONFIRMED" "" 200 "Update order 1 status to CONFIRMED"

# Step 7: Test Error Handling
echo -e "\n${BLUE}Step 7: Error Handling Tests${NC}"
echo "============================="

# Test non-existent user
api_call "GET" "$GATEWAY_URL/api/users/999" "" 404 "Get non-existent user (should return 404)"

# Test non-existent product
api_call "GET" "$GATEWAY_URL/api/products/999" "" 404 "Get non-existent product (should return 404)"

# Test order with non-existent user
INVALID_ORDER_DATA='{"userId":999,"orderItems":[{"productId":1,"quantity":1}]}'
api_call "POST" "$GATEWAY_URL/api/orders" "$INVALID_ORDER_DATA" 400 "Create order with non-existent user (should return 400)"

# Test order with non-existent product
INVALID_ORDER_DATA2='{"userId":1,"orderItems":[{"productId":999,"quantity":1}]}'
api_call "POST" "$GATEWAY_URL/api/orders" "$INVALID_ORDER_DATA2" 400 "Create order with non-existent product (should return 400)"

# Step 8: Performance Test (Simple)
echo -e "\n${BLUE}Step 8: Simple Performance Test${NC}"
echo "================================"

print_status "Running 10 concurrent requests to get all users..."
for i in {1..10}; do
    curl -s "$GATEWAY_URL/api/users" > /dev/null &
done
wait
print_success "Completed 10 concurrent requests"

# Final Summary
echo -e "\n${GREEN}🎉 API Testing Complete!${NC}"
echo "=========================="
print_success "All core functionality has been tested"
print_success "Inter-service communication is working"
print_success "Error handling is functioning correctly"
print_success "API Gateway is routing requests properly"

echo -e "\n${BLUE}Next Steps:${NC}"
echo "- Check H2 database consoles for data persistence"
echo "- Monitor application logs for any warnings"
echo "- Test additional edge cases as needed"
echo "- Ready for DevOps integration (Docker, K8s, etc.)"

echo -e "\n${BLUE}Database Consoles:${NC}"
echo "- User Service: http://localhost:8081/h2-console"
echo "- Product Service: http://localhost:8082/h2-console"
echo "- Order Service: http://localhost:8083/h2-console"
echo "- Inventory Service: http://localhost:8084/h2-console"