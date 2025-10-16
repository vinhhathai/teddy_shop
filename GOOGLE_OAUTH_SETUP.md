# Hướng dẫn cấu hình Google OAuth

## 1. Tạo Google OAuth Client ID

### Bước 1: Truy cập Google Cloud Console
1. Đi đến [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có

### Bước 2: Kích hoạt Google+ API
1. Trong Google Cloud Console, đi đến "APIs & Services" > "Library"
2. Tìm kiếm "Google+ API" và kích hoạt nó

### Bước 3: Tạo OAuth 2.0 Client ID
1. Đi đến "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. Chọn "Web application"
4. Đặt tên cho OAuth client
5. Thêm các Authorized JavaScript origins:
   - `http://localhost:3000`
   - `https://yourdomain.com` (nếu có domain production)
6. Thêm các Authorized redirect URIs:
   - `http://localhost:3000`
   - `https://yourdomain.com` (nếu có domain production)
7. Click "Create"

### Bước 4: Lấy Client ID
1. Sau khi tạo thành công, copy Client ID
2. Paste vào file `.env.local` trong frontend

## 2. Cấu hình Backend

### File: `backend/src/main/resources/application.properties`
```properties
# Google OAuth Configuration
google.oauth.client-id=YOUR_GOOGLE_CLIENT_ID_HERE
```

## 3. Cấu hình Frontend

### File: `frontend/.env.local`
```env
# Google OAuth Configuration
NEXT_PUBLIC_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID_HERE

# Backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 4. Cấu trúc Database

Đảm bảo bảng `users` có cột `google_id`:

```sql
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;
```

## 5. Test tính năng

1. Khởi động backend: `mvn spring-boot:run`
2. Khởi động frontend: `npm run dev`
3. Truy cập `http://localhost:3000/auth/login`
4. Click vào nút "Sign in with Google"
5. Đăng nhập bằng tài khoản Google
6. Kiểm tra xem user được tạo trong database

## 6. Lưu ý quan trọng

- **Bảo mật**: Không commit Google Client ID vào git repository
- **Production**: Cập nhật authorized origins và redirect URIs cho domain production
- **HTTPS**: Trong production, đảm bảo sử dụng HTTPS
- **CORS**: Đảm bảo backend cho phép requests từ frontend domain

## 7. Troubleshooting

### Lỗi "Invalid client"
- Kiểm tra Client ID có đúng không
- Kiểm tra authorized origins có chứa domain hiện tại không

### Lỗi CORS
- Kiểm tra cấu hình CORS trong backend
- Đảm bảo frontend domain được cho phép

### Google Sign-In button không hiển thị
- Kiểm tra console browser có lỗi JavaScript không
- Kiểm tra Google Client ID có được load đúng không
- Kiểm tra network có block Google APIs không

## 8. API Endpoints

### POST `/api/auth/google`
Request body:
```json
{
  "idToken": "google_id_token_here"
}
```

Response:
```json
{
  "token": "jwt_token",
  "id": 1,
  "username": "user123",
  "email": "user@gmail.com",
  "fullName": "User Name",
  "roles": ["ROLE_USER"]
}
```