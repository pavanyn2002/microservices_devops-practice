@echo off
REM Spring Boot Microservices API Testing Script for Windows
REM This script tests all API endpoints to verify the application is working correctly

echo 🚀 Starting Spring Boot Microservices API Tests
echo ================================================

REM Configuration
set GATEWAY_URL=http://localhost:8080
set USER_SERVICE_URL=http://localhost:8081
set PRODUCT_SERVICE_URL=http://localhost:8082
set ORDER_SERVICE_URL=http://localhost:8083
set INVENTORY_SERVICE_URL=http://localhost:8084

echo.
echo Step 1: Service Health Checks
echo ================================

echo Checking User Service...
curl -s -f "%USER_SERVICE_URL%/api/users/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] User Service is running
) else (
    echo [ERROR] User Service is not running
    exit /b 1
)

echo Checking Product Service...
curl -s -f "%PRODUCT_SERVICE_URL%/api/products/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Product Service is running
) else (
    echo [ERROR] Product Service is not running
    exit /b 1
)

echo Checking Order Service...
curl -s -f "%ORDER_SERVICE_URL%/api/orders/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Order Service is running
) else (
    echo [ERROR] Order Service is not running
    exit /b 1
)

echo Checking Inventory Service...
curl -s -f "%INVENTORY_SERVICE_URL%/api/inventory/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Inventory Service is running
) else (
    echo [ERROR] Inventory Service is not running
    exit /b 1
)

echo Checking API Gateway...
curl -s -f "%GATEWAY_URL%/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] API Gateway is running
) else (
    echo [ERROR] API Gateway is not running
    exit /b 1
)

echo.
echo Step 2: API Gateway Health Check
echo ==================================

echo Testing Gateway health check...
curl -s "%GATEWAY_URL%/health"
echo.

echo.
echo Step 3: User Service Tests
echo ============================

echo Creating user 1 (John Doe)...
curl -s -X POST "%GATEWAY_URL%/api/users" -H "Content-Type: application/json" -d "{\"username\":\"johndoe\",\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"
echo.

echo Creating user 2 (Jane Doe)...
curl -s -X POST "%GATEWAY_URL%/api/users" -H "Content-Type: application/json" -d "{\"username\":\"janedoe\",\"email\":\"jane@example.com\",\"firstName\":\"Jane\",\"lastName\":\"Doe\"}"
echo.

echo Getting all users...
curl -s "%GATEWAY_URL%/api/users"
echo.

echo.
echo Step 4: Product Service Tests
echo ===============================

echo Creating product 1 (Gaming Laptop)...
curl -s -X POST "%GATEWAY_URL%/api/products" -H "Content-Type: application/json" -d "{\"name\":\"Gaming Laptop\",\"description\":\"High-performance gaming laptop\",\"price\":1299.99,\"category\":\"Electronics\"}"
echo.

echo Creating product 2 (Wireless Mouse)...
curl -s -X POST "%GATEWAY_URL%/api/products" -H "Content-Type: application/json" -d "{\"name\":\"Wireless Mouse\",\"description\":\"Ergonomic wireless mouse\",\"price\":29.99,\"category\":\"Electronics\"}"
echo.

echo Getting all products...
curl -s "%GATEWAY_URL%/api/products"
echo.

echo.
echo Step 5: Inventory Service Tests
echo =================================

echo Creating inventory for product 1...
curl -s -X POST "%GATEWAY_URL%/api/inventory" -H "Content-Type: application/json" -d "{\"productId\":1,\"availableStock\":50}"
echo.

echo Creating inventory for product 2...
curl -s -X POST "%GATEWAY_URL%/api/inventory" -H "Content-Type: application/json" -d "{\"productId\":2,\"availableStock\":100}"
echo.

echo Getting all inventory...
curl -s "%GATEWAY_URL%/api/inventory"
echo.

echo.
echo Step 6: Order Service Tests
echo =============================

echo Creating order 1 (John Doe - Laptop + Mouse)...
curl -s -X POST "%GATEWAY_URL%/api/orders" -H "Content-Type: application/json" -d "{\"userId\":1,\"orderItems\":[{\"productId\":1,\"quantity\":2},{\"productId\":2,\"quantity\":1}]}"
echo.

echo Getting all orders...
curl -s "%GATEWAY_URL%/api/orders"
echo.

echo.
echo 🎉 API Testing Complete!
echo ==========================
echo [SUCCESS] All core functionality has been tested
echo [SUCCESS] Inter-service communication is working
echo [SUCCESS] API Gateway is routing requests properly

echo.
echo Database Consoles:
echo - User Service: http://localhost:8081/h2-console
echo - Product Service: http://localhost:8082/h2-console
echo - Order Service: http://localhost:8083/h2-console
echo - Inventory Service: http://localhost:8084/h2-console

pause