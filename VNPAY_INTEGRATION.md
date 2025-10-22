# 🚀 Hướng dẫn tích hợp VNPay Payment Gateway

## ✅ Đã hoàn thành

### Backend (Spring Boot):
1. ✅ `VNPayConfig.java` - Cấu hình VNPay
2. ✅ `VNPayUtil.java` - Utility cho mã hóa và hash
3. ✅ `VNPayService.java` - Service xử lý thanh toán
4. ✅ `PaymentController.java` - API endpoints
5. ✅ `VNPayPaymentRequest/Response.java` - DTOs
6. ✅ `application.properties` - Cấu hình credentials

### Frontend (Next.js):
1. ✅ `paymentApi.ts` - API client
2. ✅ `/payment/vnpay-return/page.tsx` - Trang callback
3. ✅ `/checkout/page.tsx` - Trang thanh toán (đã cập nhật)

## 📝 Cách sử dụng

### 1. Test VNPay Sandbox

VNPay cung cấp môi trường test miễn phí:

**URL:** https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

**Test Credentials (đã config sẵn):**
- TMN Code: `DEMOSANDBOX`
- Hash Secret: `DEMOSANDBOXSECRET`

**Thẻ test:**
```
Ngân hàng: NCB
Số thẻ: 9704198526191432198
Tên chủ thẻ: NGUYEN VAN A
Ngày phát hành: 07/15
Mật khẩu OTP: 123456
```

### 2. Flow thanh toán

```
1. User chọn sản phẩm → Giỏ hàng
2. Checkout → Nhập thông tin giao hàng
3. Chọn "Thanh toán qua VNPay"
4. Chọn phương thức (QR, ATM, Visa...)
5. Click "Đặt hàng"
6. → Redirect đến VNPay
7. Nhập thông tin thẻ / Quét QR
8. Thanh toán thành công
9. → Redirect về /payment/vnpay-return
10. Hiển thị kết quả + Cập nhật trạng thái đơn hàng
```

### 3. API Endpoints

#### Create Payment URL
```http
POST /api/payment/vnpay/create
Content-Type: application/json

{
  "orderId": 123,
  "amount": 500000,
  "orderInfo": "Thanh toán đơn hàng #123",
  "locale": "vn",
  "bankCode": "VNPAYQR"
}

Response:
{
  "code": "00",
  "message": "Success",
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
}
```

#### Process Return Callback
```http
GET /api/payment/vnpay/return?vnp_Amount=...&vnp_ResponseCode=00&...

Response:
{
  "code": "00",
  "message": "Payment successful",
  "orderId": "123",
  "amount": "50000000",
  "transactionNo": "14015551",
  "bankCode": "NCB",
  "payDate": "20251020120000"
}
```

#### Get Payment Methods
```http
GET /api/payment/methods

Response:
{
  "cod": { ... },
  "vnpay": {
    "name": "Thanh toán qua VNPay",
    "enabled": true,
    "banks": {
      "VNPAYQR": "QR Code",
      "VNBANK": "ATM",
      "INTCARD": "Visa/Master"
    }
  }
}
```

### 4. Test ngay

**Bước 1:** Start backend
```bash
cd backend
mvn spring-boot:run
```

**Bước 2:** Start frontend
```bash
cd frontend
npm run dev
```

**Bước 3:** Test flow
1. Vào http://localhost:3000
2. Đăng nhập/Đăng ký
3. Thêm sản phẩm vào giỏ
4. Checkout
5. Chọn "Thanh toán qua VNPay"
6. Đặt hàng
7. Nhập thẻ test (xem trên)
8. Xác nhận OTP: 123456
9. ✅ Hoàn thành!

### 5. Response Codes

| Code | Meaning |
|------|---------|
| 00 | Thành công |
| 07 | Trừ tiền thành công (nghi ngờ gian lận) |
| 09 | Chưa đăng ký Internet Banking |
| 10 | Xác thực sai quá 3 lần |
| 11 | Hết hạn thanh toán (15 phút) |
| 12 | Thẻ bị khóa |
| 24 | Khách hàng hủy |
| 51 | Không đủ số dư |
| 65 | Vượt hạn mức |
| 75 | Ngân hàng bảo trì |

### 6. Production Setup

Khi deploy production, cần:

1. **Đăng ký VNPay merchant:**
   - Website: https://vnpay.vn
   - Điền form đăng ký doanh nghiệp
   - Chờ duyệt (3-5 ngày)
   - Nhận TMN Code và Hash Secret

2. **Cập nhật application.properties:**
```properties
vnpay.url=https://vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=https://yourdomain.com/payment/vnpay-return
vnpay.tmn-code=YOUR_ACTUAL_TMN_CODE
vnpay.hash-secret=YOUR_ACTUAL_HASH_SECRET
```

3. **Setup IPN URL:**
   - Login VNPay merchant portal
   - Cấu hình IPN URL: `https://yourdomain.com/api/payment/vnpay/ipn`
   - IPN dùng để nhận webhook từ VNPay

4. **SSL Certificate:**
   - VNPay yêu cầu HTTPS
   - Setup SSL cho domain

### 7. Debugging

**Check logs:**
```bash
# Backend logs
tail -f logs/shop-gau-bong.json

# Search payment logs
grep "VNPay" logs/shop-gau-bong.json
```

**Common issues:**

1. **Invalid signature:**
   - Check hash secret đúng chưa
   - Check encoding UTF-8

2. **Order not found:**
   - Check orderId có tồn tại trong DB
   - Check order status

3. **Payment URL not generated:**
   - Check VNPay config
   - Check network connectivity

### 8. Security Notes

- ✅ Hash secret được mã hóa HMAC-SHA512
- ✅ Signature verification trên return URL
- ✅ Amount validation
- ✅ Order ID validation
- ⚠️ **KHÔNG** commit hash secret vào git
- ⚠️ Dùng environment variables cho production

### 9. Monitoring

**Metrics to track:**
- Payment success rate
- Average payment time
- Failed payment reasons
- Return callback latency

**Setup alerts for:**
- High failure rate (>10%)
- Signature validation failures
- IPN callback failures

## 🎉 Kết luận

VNPay đã được tích hợp hoàn chỉnh và sẵn sàng test!

**Ưu điểm:**
- ✅ Dễ tích hợp
- ✅ Sandbox miễn phí
- ✅ Nhiều phương thức thanh toán
- ✅ Document rõ ràng
- ✅ Phổ biến tại VN

**Next steps:**
1. Test với thẻ sandbox
2. Xử lý các edge cases
3. Add loading states
4. Improve error messages
5. Add payment history
6. Setup production

## 📚 Resources

- VNPay Docs: https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/
- VNPay Support: support@vnpay.vn
- Hotline: 1900 55 55 77
