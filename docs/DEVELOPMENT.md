# Development Guide

This guide provides information for developers working on the Spring Boot Microservices Sample project.

## Development Environment Setup

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**
- **Git**
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)
- **Docker** (optional, for containerized development)
- **Postman** (for API testing)

### IDE Configuration

#### IntelliJ IDEA
1. Import the project as a Maven project
2. Set Project SDK to Java 17
3. Enable annotation processing
4. Install Spring Boot plugin
5. Configure code style (Google Java Style recommended)

#### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure Java runtime to Java 17
4. Set up Maven integration

### Environment Variables
Create a `.env` file in the project root:
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=admin
DB_PASSWORD=secret

# Service URLs (for inter-service communication)
USER_SERVICE_URL=http://localhost:8084
PRODUCT_SERVICE_URL=http://localhost:8083
INVENTORY_SERVICE_URL=http://localhost:8081
ORDER_SERVICE_URL=http://localhost:8082
API_GATEWAY_URL=http://localhost:8080

# Logging
LOG_LEVEL=DEBUG
```

## Project Structure

```
spring-boot-microservices-sample/
├── api-gateway/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/gateway/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   └── GatewayApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── user-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/user/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   ├── config/
│   │   │   │   └── UserServiceApplication.java
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
└── [similar structure for other services]
```

## Coding Standards

### Java Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Keep methods small and focused
- Add JavaDoc for public APIs
- Use proper exception handling

### Spring Boot Best Practices
- Use `@RestController` for REST endpoints
- Implement proper validation with `@Valid`
- Use DTOs for API requests/responses
- Separate business logic in service layer
- Use repository pattern for data access

### Example Controller
```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

## Database Development

### H2 Database Configuration
Each service uses H2 in-memory database for development:

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

### Database Migration
For production, use Flyway or Liquibase for database migrations:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## Testing Strategy

### Unit Testing
- Use JUnit 5 for unit tests
- Mock dependencies with Mockito
- Test service layer logic thoroughly
- Aim for 80%+ code coverage

### Integration Testing
- Use `@SpringBootTest` for integration tests
- Test complete request/response cycles
- Use TestContainers for database testing

### Example Unit Test
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
        User savedUser = new User(1L, "John Doe", "john@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserDto result = userService.createUser(request);
        
        // Then
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }
}
```

### Example Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "jane@example.com");
        
        // When
        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(
            "/api/users", request, UserDto.class);
        
        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().getName()).isEqualTo("Jane Doe");
    }
}
```

## API Development

### Request/Response DTOs
Create separate DTOs for requests and responses:

```java
// Request DTO
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;
    
    // constructors, getters, setters
}

// Response DTO
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    
    // constructors, getters, setters
}
```

### Error Handling
Implement global exception handling:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

## Service Communication

### RestTemplate Configuration
Configure RestTemplate for inter-service communication:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "services")
    public ServiceUrls serviceUrls() {
        return new ServiceUrls();
    }
}
```

### Service Client Example
```java
@Service
public class ProductServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceUrls serviceUrls;
    
    public ProductServiceClient(RestTemplate restTemplate, ServiceUrls serviceUrls) {
        this.restTemplate = restTemplate;
        this.serviceUrls = serviceUrls;
    }
    
    public ProductDto getProduct(Long productId) {
        String url = serviceUrls.getProductService() + "/api/products/" + productId;
        return restTemplate.getForObject(url, ProductDto.class);
    }
}
```

## Configuration Management

### Application Properties
Use different property files for different environments:

```properties
# application.properties (common)
spring.application.name=user-service
server.port=8084
management.endpoints.web.exposure.include=health,info,metrics

# application-dev.properties (development)
logging.level.com.example=DEBUG
spring.h2.console.enabled=true

# application-prod.properties (production)
logging.level.com.example=INFO
spring.datasource.url=${DATABASE_URL}
```

### Configuration Classes
```java
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private String name;
    private String version;
    private Security security = new Security();
    
    @Data
    public static class Security {
        private boolean enabled = true;
        private String secretKey;
    }
}
```

## Debugging

### Local Debugging
1. Set breakpoints in your IDE
2. Run services in debug mode
3. Use remote debugging for containerized services

### Remote Debugging with Docker
```dockerfile
# Add to Dockerfile for debugging
EXPOSE 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
```

### Logging Configuration
```properties
# Logging configuration
logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.file.name=logs/application.log
```

## Performance Optimization

### JVM Tuning
```bash
# JVM options for production
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Optimization
- Use connection pooling
- Implement proper indexing
- Use pagination for large datasets
- Consider caching frequently accessed data

### Caching
```java
@Service
@EnableCaching
public class UserService {
    
    @Cacheable("users")
    public UserDto getUserById(Long id) {
        // Implementation
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        // Implementation
    }
}
```

## Git Workflow

### Branch Strategy
- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `hotfix/*` - Critical fixes

### Commit Messages
Follow conventional commit format:
```
feat: add user registration endpoint
fix: resolve null pointer exception in order service
docs: update API documentation
test: add integration tests for product service
```

### Pre-commit Hooks
Set up pre-commit hooks for:
- Code formatting
- Unit test execution
- Static code analysis

## Continuous Integration

### GitHub Actions Example
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run tests
      run: mvn clean test
    - name: Build Docker images
      run: |
        docker build -t user-service ./user-service
        docker build -t product-service ./product-service
```

## Monitoring and Observability

### Health Checks
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Custom health check logic
        return Health.up()
            .withDetail("database", "available")
            .withDetail("external-service", "connected")
            .build();
    }
}
```

### Metrics
```java
@RestController
public class UserController {
    
    private final MeterRegistry meterRegistry;
    private final Counter userCreationCounter;
    
    public UserController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userCreationCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
    }
    
    @PostMapping("/api/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        userCreationCounter.increment();
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

## Troubleshooting

### Common Issues
1. **Port conflicts**: Check if ports are already in use
2. **Database connection issues**: Verify database configuration
3. **Service discovery problems**: Check service URLs and network connectivity
4. **Memory issues**: Monitor heap usage and adjust JVM settings

### Debugging Tools
- Spring Boot Actuator endpoints
- JProfiler or VisualVM for performance analysis
- Docker logs for containerized services
- Application logs with proper log levels