# 🤖 HƯỚNG DẪN SETUP CHATBOT GPT

## ✅ Đã hoàn thành

### Backend:
- ✅ Thêm dependency OpenAI GPT vào `pom.xml`
- ✅ Tạo Entity `ChatMessage` để lưu lịch sử chat
- ✅ Tạo Repository `ChatMessageRepository`
- ✅ Tạo Service `ChatbotService` với GPT-3.5-turbo
- ✅ Tạo Controller `ChatbotController` với các endpoints:
  - `POST /api/chatbot/chat` - Gửi tin nhắn
  - `GET /api/chatbot/history` - Lấy lịch sử
  - `DELETE /api/chatbot/history` - Xóa lịch sử
  - `GET /api/chatbot/health` - Health check

### Frontend:
- ✅ Tạo component `ChatbotWidget` 
- ✅ Tích hợp vào `layout.tsx`
- ✅ UI đẹp với Tailwind CSS
- ✅ Floating button ở góc phải màn hình

---

## 🔑 BƯỚC 1: LẤY OPENAI API KEY

### 1.1. Đăng ký OpenAI
1. Truy cập: https://platform.openai.com/signup
2. Đăng ký tài khoản (có thể dùng Google)
3. Xác thực email

### 1.2. Tạo API Key
1. Đăng nhập: https://platform.openai.com/
2. Click vào avatar → "View API keys"
3. Click "Create new secret key"
4. Đặt tên: "teddy-bear-shop"
5. **Copy key ngay** (chỉ hiển thị 1 lần!)

**Ví dụ key:** `sk-proj-abc123...xyz789`

### 1.3. Nạp tiền (Credit)
- OpenAI yêu cầu nạp tối thiểu $5
- Truy cập: https://platform.openai.com/account/billing
- Click "Add payment method"
- Nạp tối thiểu $5 USD

**Lưu ý:** 
- $5 có thể dùng được rất lâu (hàng nghìn requests)
- GPT-3.5-turbo rẻ nhất: ~$0.002/1000 tokens
- Có thể set usage limit để tránh vượt ngân sách

---

## ⚙️ BƯỚC 2: CẤU HÌNH BACKEND

### 2.1. Cập nhật application.properties

Mở file: `backend/src/main/resources/application.properties`

Tìm dòng:
```properties
openai.api-key=YOUR_OPENAI_API_KEY_HERE
```

Thay bằng key của bạn:
```properties
openai.api-key=sk-proj-abc123...xyz789
```

### 2.2. Tuỳ chỉnh (optional)

```properties
# Model: gpt-3.5-turbo (rẻ) hoặc gpt-4 (đắt nhưng thông minh hơn)
openai.model=gpt-3.5-turbo

# Max tokens: Độ dài response (500 tokens ~ 375 từ)
openai.max-tokens=500

# Temperature: Độ sáng tạo (0.0 = giống nhau, 1.0 = sáng tạo)
openai.temperature=0.7
```

---

## 🚀 BƯỚC 3: BUILD & CHẠY

### 3.1. Backend

```bash
cd backend

# Clean và build lại
mvn clean install -DskipTests

# Chạy backend
mvn spring-boot:run
```

**Kiểm tra:**
- Backend chạy tại: http://localhost:8080
- Test health: http://localhost:8080/api/chatbot/health

### 3.2. Frontend

```bash
cd frontend

# Install dependencies (nếu chưa)
npm install

# Chạy frontend
npm run dev
```

**Kiểm tra:**
- Frontend chạy tại: http://localhost:3000
- Sẽ thấy icon chat màu xanh ở góc phải dưới

---

## 🧪 BƯỚC 4: TEST CHATBOT

### 4.1. Test trên UI

1. Mở http://localhost:3000
2. **Đăng nhập** (bắt buộc)
3. Click vào icon 💬 ở góc phải dưới
4. Gửi tin nhắn test:
   - "Xin chào"
   - "Gấu bông có những loại nào?"
   - "Giá bao nhiêu?"
   - "Làm sao để đặt hàng?"

### 4.2. Test với Postman

```
Method: POST
URL: http://localhost:8080/api/chatbot/chat
Headers:
  Content-Type: application/json
  Authorization: Bearer {YOUR_JWT_TOKEN}
Body (raw JSON):
{
  "message": "Xin chào! Shop có gấu bông gì đẹp không?"
}

Expected Response (200 OK):
{
  "id": null,
  "userMessage": "Xin chào! Shop có gấu bông gì đẹp không?",
  "botResponse": "Xin chào! Cảm ơn bạn đã quan tâm đến Shop Gấu Bông...",
  "createdAt": "2025-10-21T20:30:00"
}
```

---

## 📊 BƯỚC 5: MONITORING & COST

### 5.1. Theo dõi Usage
1. Truy cập: https://platform.openai.com/usage
2. Xem số request và cost
3. Set usage limit nếu cần

### 5.2. Estimated Cost

**Với GPT-3.5-turbo:**
- Input: $0.0015 / 1K tokens
- Output: $0.002 / 1K tokens

**Ví dụ:**
- 1 chat session: ~200 tokens input + 300 tokens output
- Cost per session: ~$0.001 (23 VND)
- 1000 sessions: ~$1 (23,000 VND)

**⇒ $5 = ~5000 chat sessions = đủ dùng lâu!**

---

## 🎨 BƯỚC 6: CUSTOMIZE CHATBOT

### 6.1. Thay đổi System Prompt

Mở: `backend/src/main/java/com/gaubong/teddybearshop/service/ChatbotService.java`

Tìm dòng 38-44, thay đổi `SYSTEM_PROMPT`:

```java
private static final String SYSTEM_PROMPT = 
    "Bạn là trợ lý ảo của Shop Gấu Bông... " +
    "Hãy trả lời vui vẻ, nhiệt tình...";
```

### 6.2. Thay đổi UI

Mở: `frontend/src/components/ChatbotWidget.tsx`

**Thay màu:**
```tsx
// Dòng 134: Thay bg-blue-600 thành bg-pink-600
className="bg-pink-600 text-white..."
```

**Thay icon:**
```tsx
// Dòng 147: Thay emoji
<h3>🐻 Trợ lý Gấu Bông</h3>
```

---

## ❗ TROUBLESHOOTING

### Lỗi: "OpenAI API key chưa được cấu hình"
**Nguyên nhân:** Chưa thay API key trong application.properties  
**Fix:** Thay `YOUR_OPENAI_API_KEY_HERE` bằng key thật

### Lỗi: "401 Unauthorized" khi gửi chat
**Nguyên nhân:** Chưa đăng nhập  
**Fix:** Đăng nhập trước khi dùng chatbot

### Lỗi: "insufficient_quota"
**Nguyên nhân:** Hết credit OpenAI hoặc chưa nạp tiền  
**Fix:** Nạp tiền vào tài khoản OpenAI

### Lỗi: "Request timeout"
**Nguyên nhân:** Mạng chậm hoặc OpenAI overload  
**Fix:** Tăng timeout trong ChatbotService (dòng 56)

### Bot trả lời không đúng về sản phẩm
**Nguyên nhân:** Bot chưa biết về sản phẩm thật  
**Fix:** Thêm thông tin sản phẩm vào SYSTEM_PROMPT hoặc integrate với ProductService

---

## 🎯 TÍNH NĂNG NÂNG CAO (OPTIONAL)

### 1. Thêm thông tin sản phẩm vào context
```java
// Lấy danh sách sản phẩm và thêm vào prompt
String productInfo = productService.getAllProducts()
    .stream()
    .map(p -> p.getName() + ": " + p.getPrice())
    .collect(Collectors.joining("\n"));

String enhancedPrompt = SYSTEM_PROMPT + "\n\nSản phẩm hiện có:\n" + productInfo;
```

### 2. Voice input/output
- Thêm Web Speech API cho input
- Thêm Text-to-Speech cho output

### 3. Multi-language
- Detect ngôn ngữ user
- Trả lời bằng ngôn ngữ tương ứng

### 4. Analytics
- Track số lượng messages
- Track câu hỏi phổ biến
- Improve system prompt dựa vào feedback

---

## 📞 SUPPORT

Nếu gặp vấn đề:
1. Check logs backend: Terminal chạy `mvn spring-boot:run`
2. Check console frontend: F12 → Console tab
3. Test API trực tiếp với Postman
4. Check OpenAI usage: https://platform.openai.com/usage

---

## 🎉 DONE!

Chatbot đã sẵn sàng! Giờ khách hàng có thể chat trực tiếp với AI để được tư vấn về sản phẩm gấu bông 24/7! 🤖🧸
