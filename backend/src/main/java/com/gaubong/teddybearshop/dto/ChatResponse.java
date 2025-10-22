package com.gaubong.teddybearshop.dto;

import java.time.LocalDateTime;

public class ChatResponse {
    private Long id;
    private String userMessage;
    private String botResponse;
    private LocalDateTime createdAt;

    public ChatResponse() {}

    public ChatResponse(Long id, String userMessage, String botResponse, LocalDateTime createdAt) {
        this.id = id;
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getBotResponse() {
        return botResponse;
    }

    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
