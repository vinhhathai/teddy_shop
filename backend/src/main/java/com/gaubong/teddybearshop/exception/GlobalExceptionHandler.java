package com.gaubong.teddybearshop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = getVietnameseErrorMessage(fieldName, error.getDefaultMessage());
            fieldErrors.put(fieldName, errorMessage);
        });
        
        // Clean logging - chỉ log field và message chính
        String firstError = fieldErrors.entrySet().iterator().next().getKey() + ": " + 
                           fieldErrors.entrySet().iterator().next().getValue();
        logger.error("Validation failed - {}", firstError);
        
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);
        response.put("status", "VALIDATION_ERROR");
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    private String getVietnameseErrorMessage(String fieldName, String defaultMessage) {
        // Chuyển đổi error messages sang tiếng Việt
        if (defaultMessage.contains("size must be between")) {
            if (fieldName.equals("fullName")) {
                return "Họ tên phải từ 1-50 ký tự";
            }
            if (fieldName.equals("username")) {
                return "Tên đăng nhập phải từ 3-20 ký tự";
            }
            if (fieldName.equals("password")) {
                return "Mật khẩu phải từ 6-100 ký tự";
            }
        }
        
        if (defaultMessage.contains("must not be blank")) {
            switch (fieldName) {
                case "username": return "Tên đăng nhập không được để trống";
                case "password": return "Mật khẩu không được để trống";
                case "email": return "Email không được để trống";
                case "fullName": return "Họ tên không được để trống";
                default: return "Trường này không được để trống";
            }
        }
        
        if (defaultMessage.contains("must be a well-formed email")) {
            return "Email không đúng định dạng";
        }
        
        // Fallback to default message
        return defaultMessage;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error - {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}