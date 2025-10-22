package com.gaubong.teddybearshop.controller;

import com.gaubong.teddybearshop.dto.VNPayPaymentRequest;
import com.gaubong.teddybearshop.dto.VNPayPaymentResponse;
import com.gaubong.teddybearshop.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private VNPayService vnPayService;
    
    /**
     * Create VNPay payment URL
     * POST /api/payment/vnpay/create
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createVNPayPayment(
            @RequestBody VNPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            VNPayPaymentResponse response = vnPayService.createPayment(request, httpRequest);
            
            if ("00".equals(response.getCode())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "99");
            error.put("message", "Error creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * VNPay return callback (handled by frontend)
     * GET /api/payment/vnpay/return
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            Map<String, String> result = vnPayService.processReturn(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "99");
            error.put("message", "Error processing return: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * VNPay IPN (Instant Payment Notification) callback
     * GET /api/payment/vnpay/ipn
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<?> vnpayIPN(@RequestParam Map<String, String> params) {
        try {
            Map<String, String> result = vnPayService.processReturn(params);
            
            // Return response in VNPay format
            Map<String, String> response = new HashMap<>();
            if ("00".equals(result.get("code"))) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "99");
                response.put("Message", "Unknown error");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("RspCode", "99");
            response.put("Message", "System error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get payment methods
     * GET /api/payment/methods
     */
    @GetMapping("/methods")
    public ResponseEntity<?> getPaymentMethods() {
        Map<String, Object> methods = new HashMap<>();
        methods.put("cod", Map.of(
            "name", "Thanh toán khi nhận hàng (COD)",
            "description", "Thanh toán bằng tiền mặt khi nhận hàng",
            "enabled", true
        ));
        methods.put("vnpay", Map.of(
            "name", "Thanh toán qua VNPay",
            "description", "Thanh toán qua thẻ ATM, Visa, MasterCard, QR Code",
            "enabled", true,
            "banks", getBankCodes()
        ));
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Get supported bank codes
     */
    private Map<String, String> getBankCodes() {
        Map<String, String> banks = new HashMap<>();
        banks.put("VNPAYQR", "Thanh toán qua ứng dụng hỗ trợ VNPAYQR");
        banks.put("VNBANK", "Thanh toán qua ATM-Tài khoản ngân hàng nội địa");
        banks.put("INTCARD", "Thanh toán qua thẻ quốc tế");
        banks.put("", "Cổng thanh toán VNPay (tất cả phương thức)");
        return banks;
    }
}
