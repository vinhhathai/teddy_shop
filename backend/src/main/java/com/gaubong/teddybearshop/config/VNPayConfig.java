package com.gaubong.teddybearshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@Configuration
public class VNPayConfig {
    
    // VNPay Sandbox Credentials (Public for testing)
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    @Value("${vnpay.return-url:http://localhost:3000/payment/vnpay-return}")
    private String vnpReturnUrl;
    
    @Value("${vnpay.tmn-code:DEMO}")
    private String vnpTmnCode;
    
    @Value("${vnpay.hash-secret:DEMOSECRET}")
    private String vnpHashSecret;
    
    @Value("${vnpay.api-url:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}")
    private String vnpApiUrl;
    
    public String getVnpUrl() {
        return vnpUrl;
    }
    
    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }
    
    public String getVnpTmnCode() {
        return vnpTmnCode;
    }
    
    public String getVnpHashSecret() {
        return vnpHashSecret;
    }
    
    public String getVnpApiUrl() {
        return vnpApiUrl;
    }
    
    // Generate VNPay transaction reference
    public String getRandomNumber(int len) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    // Get current date time in VNPay format
    public String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(cal.getTime());
    }
    
    // Get expire date time (15 minutes from now)
    public String getExpireDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        cal.add(Calendar.MINUTE, 15);
        return formatter.format(cal.getTime());
    }
}
