# 🧸 Shop Gấu Bông - Teddy Bear E-commerce Platform

Một hệ thống thương mại điện tử hoàn chỉnh cho việc bán gấu bông, được xây dựng với Spring Boot backend và Next.js frontend.

## 📋 Tổng quan

Shop Gấu Bông là một ứng dụng web thương mại điện tử được thiết kế để bán các sản phẩm gấu bông. Hệ thống bao gồm:

- **Backend**: Spring Boot với Spring Security, JPA/Hibernate
- **Frontend**: Next.js với TypeScript, Tailwind CSS
- **Database**: MySQL/PostgreSQL
- **Authentication**: JWT-based authentication
- **Features**: Quản lý sản phẩm, đơn hàng, người dùng, giỏ hàng

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────┐    HTTP/REST API    ┌─────────────────┐
│   Next.js       │ ◄─────────────────► │   Spring Boot   │
│   Frontend      │                     │   Backend       │
│                 │                     │                 │
│ - React Pages   │                     │ - REST APIs     │
│ - TypeScript    │                     │ - JWT Auth      │
│ - Tailwind CSS  │                     │ - JPA/Hibernate │
│ - Axios         │                     │ - Spring Security│
└─────────────────┘                     └─────────────────┘
                                                  │
                                                  ▼
                                        ┌─────────────────┐
                                        │   MySQL/        │
                                        │   PostgreSQL    │
                                        │   Database      │
                                        └─────────────────┘
```

## 🚀 Tính năng chính

### 👤 Quản lý người dùng
- Đăng ký/Đăng nhập với JWT authentication
- Phân quyền người dùng (USER, ADMIN)
- Quản lý thông tin cá nhân
- Đổi mật khẩu

### 🛍️ Quản lý sản phẩm
- Hiển thị danh sách sản phẩm với phân trang
- Tìm kiếm và lọc sản phẩm
- Quản lý danh mục sản phẩm
- CRUD operations cho admin

### 🛒 Giỏ hàng & Đặt hàng
- Thêm/xóa sản phẩm vào giỏ hàng
- Quản lý số lượng sản phẩm
- Checkout và tạo đơn hàng
- Theo dõi trạng thái đơn hàng

### 👨‍💼 Admin Dashboard
- Thống kê doanh thu và đơn hàng
- Quản lý sản phẩm và kho hàng
- Quản lý đơn hàng và người dùng
- Báo cáo và phân tích

## 📁 Cấu trúc thư mục

```
gaubong/
├── backend/                 # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/gaubong/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST Controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA Entities
│   │       ├── repository/  # JPA Repositories
│   │       ├── service/     # Business Logic
│   │       └── util/        # Utility classes
│   ├── src/main/resources/
│   │   ├── application.yml  # Application configuration
│   │   └── data.sql        # Sample data
│   └── pom.xml             # Maven dependencies
│
├── frontend/               # Next.js Frontend
│   ├── src/
│   │   ├── app/           # Next.js App Router pages
│   │   ├── components/    # Reusable components
│   │   ├── contexts/      # React contexts
│   │   ├── lib/          # API client and utilities
│   │   └── types/        # TypeScript type definitions
│   ├── public/           # Static assets
│   ├── package.json      # NPM dependencies
│   └── tailwind.config.js # Tailwind CSS config
│
└── README.md             # Project documentation
```

## 🛠️ Công nghệ sử dụng

### Backend
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database ORM
- **MySQL/PostgreSQL** - Database
- **JWT** - Token-based authentication
- **Maven** - Dependency management

### Frontend
- **Next.js 15** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **React Query** - Data fetching
- **Axios** - HTTP client
- **Lucide React** - Icons

## 📦 Cài đặt và chạy

### Yêu cầu hệ thống
- Java 17+
- Node.js 18+
- MySQL/PostgreSQL
- Maven
- npm/yarn

### 1. Clone repository
```bash
git clone <repository-url>
cd gaubong
```

### 2. Cài đặt Backend
```bash
cd backend

# Cấu hình database trong application.yml
# Cập nhật thông tin kết nối database

# Chạy ứng dụng
mvn spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### 3. Cài đặt Frontend
```bash
cd frontend

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:3000`

## 🔧 Cấu hình

### Backend Configuration (application.yml)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gaubong_db
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

jwt:
  secret: your-secret-key
  expiration: 86400000 # 24 hours
```

### Frontend Configuration
Cập nhật API base URL trong `src/lib/api.ts`:
```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/signin` - Đăng nhập
- `POST /api/auth/signup` - Đăng ký

### Product Endpoints
- `GET /api/products` - Lấy danh sách sản phẩm
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm
- `POST /api/products` - Tạo sản phẩm mới (Admin)
- `PUT /api/products/{id}` - Cập nhật sản phẩm (Admin)
- `DELETE /api/products/{id}` - Xóa sản phẩm (Admin)

### Order Endpoints
- `GET /api/orders` - Lấy danh sách đơn hàng
- `GET /api/orders/{id}` - Lấy chi tiết đơn hàng
- `POST /api/orders` - Tạo đơn hàng mới
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng (Admin)

### User Endpoints
- `GET /api/users/profile` - Lấy thông tin người dùng hiện tại
- `PUT /api/users/profile` - Cập nhật thông tin cá nhân
- `PUT /api/users/password` - Đổi mật khẩu

## 🔐 Bảo mật

- JWT-based authentication
- Password encryption với BCrypt
- Role-based authorization (USER, ADMIN)
- CORS configuration
- Input validation và sanitization

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

## 📈 Deployment

### Backend Deployment
1. Build JAR file: `mvn clean package`
2. Deploy JAR file lên server
3. Cấu hình database production
4. Chạy: `java -jar target/gaubong-backend.jar`

### Frontend Deployment
1. Build production: `npm run build`
2. Deploy static files lên CDN/hosting service
3. Cấu hình environment variables

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push branch: `git push origin feature/new-feature`
5. Tạo Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Team

- **Backend Developer**: Spring Boot, MySQL, JWT Authentication
- **Frontend Developer**: Next.js, TypeScript, Tailwind CSS
- **DevOps**: Deployment và CI/CD

## 📞 Liên hệ

- Email: support@gaubong.com
- Website: https://gaubong.com
- GitHub: https://github.com/your-username/gaubong

---

⭐ Nếu project này hữu ích, hãy cho chúng tôi một star trên GitHub!