package com.gaubong.teddybearshop.dto;

public class VNPayPaymentRequest {
    private Long orderId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private String locale; // "vn" or "en"
    private String bankCode; // Optional: specific bank code
    
    // Constructors
    public VNPayPaymentRequest() {
        this.orderType = "other";
        this.locale = "vn";
    }
    
    public VNPayPaymentRequest(Long orderId, Long amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.orderType = "other";
        this.locale = "vn";
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
}
