# 🧸 Shop Gấu Bông - Backend

Backend API cho hệ thống thương mại điện tử Shop Gấu Bông, được xây dựng với Spring Boot, Spring Security và MySQL.

## 🚀 Tính năng

### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based authorization (USER, ADMIN)
- Password encryption với BCrypt
- Token refresh mechanism

### 👤 User Management
- User registration và login
- Profile management
- Password change
- User role management

### 🛍️ Product Management
- CRUD operations cho products
- Category management
- Product search và filtering
- Inventory tracking
- Image upload support

### 📦 Order Management
- Order creation và tracking
- Order status management
- Order history
- Order cancellation
- Payment integration ready

### 🛒 Shopping Cart
- Cart item management
- Quantity updates
- Cart persistence
- Cart to order conversion

### 📊 Admin Features
- Dashboard statistics
- User management
- Product management
- Order management
- Sales reporting

## 🛠️ Công nghệ sử dụng

- **Java 17+**
- **Spring Boot 3.x** - Main framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database ORM
- **MySQL** - Primary database
- **JWT** - Token-based authentication
- **BCrypt** - Password encryption
- **Maven** - Dependency management
- **Hibernate** - ORM implementation

## 📁 Cấu trúc dự án

```
backend/
├── src/main/java/com/gaubong/
│   ├── config/                 # Configuration classes
│   │   ├── CorsConfig.java    # CORS configuration
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java # Spring Security config
│   │
│   ├── controller/            # REST Controllers
│   │   ├── AuthController.java
│   │   ├── OrderController.java
│   │   ├── ProductController.java
│   │   └── UserController.java
│   │
│   ├── dto/                   # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   └── UpdatePasswordRequest.java
│   │   └── response/
│   │       ├── JwtResponse.java
│   │       ├── MessageResponse.java
│   │       └── PageResponse.java
│   │
│   ├── entity/               # JPA Entities
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Product.java
│   │   ├── Role.java
│   │   └── User.java
│   │
│   ├── repository/           # JPA Repositories
│   │   ├── OrderRepository.java
│   │   ├── ProductRepository.java
│   │   ├── RoleRepository.java
│   │   └── UserRepository.java
│   │
│   ├── service/              # Business Logic
│   │   ├── impl/
│   │   │   ├── OrderServiceImpl.java
│   │   │   ├── ProductServiceImpl.java
│   │   │   └── UserServiceImpl.java
│   │   ├── OrderService.java
│   │   ├── ProductService.java
│   │   ├── UserDetailsImpl.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── UserService.java
│   │
│   ├── util/                 # Utility classes
│   │   └── JwtUtils.java
│   │
│   └── GaubongApplication.java # Main application class
│
├── src/main/resources/
│   ├── application.yml       # Application configuration
│   └── data.sql             # Sample data (optional)
│
├── src/test/                # Test classes
├── pom.xml                  # Maven dependencies
└── README.md               # This file
```

## 🔧 Cài đặt và chạy

### Yêu cầu hệ thống
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 1. Clone repository
```bash
git clone <repository-url>
cd gaubong/backend
```

### 2. Cấu hình Database
Tạo database MySQL:
```sql
CREATE DATABASE gaubong_db;
CREATE USER 'gaubong_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON gaubong_db.* TO 'gaubong_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Cấu hình Application
Cập nhật `src/main/resources/application.yml`:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gaubong_db
    username: gaubong_user
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

jwt:
  secret: mySecretKey
  expiration: 86400000 # 24 hours

logging:
  level:
    com.gaubong: DEBUG
    org.springframework.security: DEBUG
```

### 4. Chạy ứng dụng
```bash
# Compile và chạy
mvn spring-boot:run

# Hoặc build JAR và chạy
mvn clean package
java -jar target/gaubong-backend-1.0.0.jar
```

Backend sẽ chạy tại: `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### POST /auth/signin
Đăng nhập người dùng
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "user@example.com",
  "email": "user@example.com",
  "roles": ["ROLE_USER"]
}
```

#### POST /auth/signup
Đăng ký người dùng mới
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "fullName": "New User",
  "phoneNumber": "0123456789",
  "address": "123 Main St"
}
```

### Product Endpoints

#### GET /products
Lấy danh sách sản phẩm với phân trang và filtering
```
GET /products?page=0&size=10&search=gấu&category=teddy&sortBy=name&sortDir=asc
```

#### GET /products/{id}
Lấy chi tiết sản phẩm

#### POST /products (Admin only)
Tạo sản phẩm mới
```json
{
  "name": "Gấu Bông Teddy",
  "description": "Gấu bông dễ thương",
  "price": 299000,
  "category": "TEDDY",
  "stockQuantity": 100,
  "imageUrl": "https://example.com/image.jpg"
}
```

#### PUT /products/{id} (Admin only)
Cập nhật sản phẩm

#### DELETE /products/{id} (Admin only)
Xóa sản phẩm

### Order Endpoints

#### GET /orders
Lấy danh sách đơn hàng của user hiện tại

#### GET /orders/{id}
Lấy chi tiết đơn hàng

#### POST /orders
Tạo đơn hàng mới
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": "123 Main St",
  "paymentMethod": "COD"
}
```

#### PUT /orders/{id}/status (Admin only)
Cập nhật trạng thái đơn hàng
```json
{
  "status": "SHIPPED"
}
```

### User Endpoints

#### GET /users/profile
Lấy thông tin profile của user hiện tại

#### PUT /users/profile
Cập nhật thông tin profile
```json
{
  "fullName": "Updated Name",
  "phoneNumber": "0987654321",
  "address": "456 New St"
}
```

#### PUT /users/password
Đổi mật khẩu
```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```

## 🔐 Security Features

### JWT Authentication
- Stateless authentication
- Token expiration handling
- Automatic token validation

### Password Security
- BCrypt encryption
- Password strength validation
- Secure password change process

### Authorization
- Role-based access control
- Method-level security
- Resource-level permissions

### CORS Configuration
- Configured for frontend integration
- Secure cross-origin requests
- Credential support

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Products Table
```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    stock_quantity INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    shipping_address TEXT,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## 📊 Monitoring & Logging

### Application Logs
- Structured logging với Logback
- Different log levels cho environments
- Request/Response logging

### Health Checks
- Spring Boot Actuator endpoints
- Database connectivity checks
- Custom health indicators

### Metrics
- JVM metrics
- Application metrics
- Database metrics

## 🚀 Deployment

### Development
```bash
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package -Pprod
java -jar target/gaubong-backend-1.0.0.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/gaubong-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gaubong_db
export SPRING_DATASOURCE_USERNAME=gaubong_user
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
```

## 🔧 Configuration

### Profiles
- `dev` - Development environment
- `test` - Testing environment  
- `prod` - Production environment

### External Configuration
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Kiểm tra MySQL service đang chạy
   - Verify database credentials
   - Check network connectivity

2. **JWT Token Invalid**
   - Verify JWT secret configuration
   - Check token expiration
   - Validate token format

3. **CORS Errors**
   - Update CORS configuration
   - Check allowed origins
   - Verify request headers

### Debug Mode
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 📈 Performance Optimization

### Database Optimization
- Connection pooling
- Query optimization
- Index strategies
- Lazy loading configuration

### Caching
- Spring Cache abstraction
- Redis integration ready
- Method-level caching

### Security Optimization
- JWT token optimization
- Password hashing optimization
- Session management

## 🤝 Contributing

### Code Style
- Follow Java coding conventions
- Use meaningful variable names
- Add comprehensive JavaDoc
- Write unit tests

### Git Workflow
1. Create feature branch
2. Implement changes
3. Write tests
4. Submit pull request

## 📄 License

This project is licensed under the MIT License.

---

📞 **Support**: Nếu có vấn đề, tạo issue trên GitHub repository hoặc liên hệ backend team.