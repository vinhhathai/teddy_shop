package com.gaubong.teddybearshop.service;

import com.gaubong.teddybearshop.config.VNPayConfig;
import com.gaubong.teddybearshop.dto.VNPayPaymentRequest;
import com.gaubong.teddybearshop.dto.VNPayPaymentResponse;
import com.gaubong.teddybearshop.entity.Order;
import com.gaubong.teddybearshop.repository.OrderRepository;
import com.gaubong.teddybearshop.util.VNPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VNPayService {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayService.class);
    
    @Autowired
    private VNPayConfig vnPayConfig;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Create payment URL for VNPay
     */
    public VNPayPaymentResponse createPayment(VNPayPaymentRequest request, HttpServletRequest httpRequest) {
        try {
            // Verify order exists
            Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
            if (orderOpt.isEmpty()) {
                return new VNPayPaymentResponse("01", "Order not found", null);
            }
            
            Order order = orderOpt.get();
            
            // Build VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100)); // VNPay uses cents
            vnpParams.put("vnp_CurrCode", "VND");
            
            if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
                vnpParams.put("vnp_BankCode", request.getBankCode());
            }
            
            vnpParams.put("vnp_TxnRef", String.valueOf(order.getId())); // Use order ID as transaction reference
            vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
            vnpParams.put("vnp_OrderType", request.getOrderType());
            vnpParams.put("vnp_Locale", request.getLocale());
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            vnpParams.put("vnp_IpAddr", getIpAddress(httpRequest));
            vnpParams.put("vnp_CreateDate", vnPayConfig.getCurrentDateTime());
            vnpParams.put("vnp_ExpireDate", vnPayConfig.getExpireDateTime());
            
            // Build query string
            String queryUrl = VNPayUtil.getQueryString(vnpParams);
            String hashData = VNPayUtil.hashAllFields(vnpParams);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;
            
            logger.info("Created VNPay payment URL for order: {}", order.getId());
            
            return new VNPayPaymentResponse("00", "Success", paymentUrl);
            
        } catch (UnsupportedEncodingException e) {
            logger.error("Error creating VNPay payment URL", e);
            return new VNPayPaymentResponse("99", "Error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Process VNPay return callback
     */
    public Map<String, String> processReturn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // Get secure hash from params
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            // Build hash data
            String hashData = VNPayUtil.hashAllFields(params);
            String checkSum = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData);
            
            // Verify signature
            if (!checkSum.equals(vnpSecureHash)) {
                result.put("code", "97");
                result.put("message", "Invalid signature");
                return result;
            }
            
            // Get payment info
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            String transactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");
            
            // Check response code
            if ("00".equals(responseCode)) {
                // Payment success
                Long orderId = Long.parseLong(txnRef);
                Optional<Order> orderOpt = orderRepository.findById(orderId);
                
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    // Update order status to CONFIRMED or PAID
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    
                    logger.info("Payment successful for order: {}, transaction: {}", orderId, transactionNo);
                    
                    result.put("code", "00");
                    result.put("message", "Payment successful");
                    result.put("orderId", txnRef);
                    result.put("amount", amount);
                    result.put("transactionNo", transactionNo);
                    result.put("bankCode", bankCode);
                    result.put("payDate", payDate);
                } else {
                    result.put("code", "01");
                    result.put("message", "Order not found");
                }
            } else {
                // Payment failed
                result.put("code", responseCode);
                result.put("message", getResponseMessage(responseCode));
                logger.warn("Payment failed for order: {}, code: {}", txnRef, responseCode);
            }
            
        } catch (Exception e) {
            logger.error("Error processing VNPay return", e);
            result.put("code", "99");
            result.put("message", "System error");
        }
        
        return result;
    }
    
    /**
     * Get client IP address
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * Get response message from code
     */
    private String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10" -> "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13" -> "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24" -> "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51" -> "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default -> "Giao dịch thất bại";
        };
    }
}
