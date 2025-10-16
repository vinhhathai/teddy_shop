package com.gaubong.teddybearshop.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleOAuthRequest {
    
    @NotBlank(message = "Google ID token is required")
    private String idToken;
    
    public GoogleOAuthRequest() {}
    
    public GoogleOAuthRequest(String idToken) {
        this.idToken = idToken;
    }
    
    public String getIdToken() {
        return idToken;
    }
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}