package com.gaubong.teddybearshop.controller;

import com.gaubong.teddybearshop.dto.ChatRequest;
import com.gaubong.teddybearshop.dto.ChatResponse;
import com.gaubong.teddybearshop.entity.ChatMessage;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.service.ChatbotService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user) {
        
        try {
            logger.info("POST /chatbot/chat - User: {}, Message: {}", 
                user != null ? user.getUsername() : "anonymous", 
                request.getMessage());

            String response = chatbotService.getChatResponse(request.getMessage(), user);
            
            logger.info("POST /chatbot/chat - Response generated successfully");
            
            return ResponseEntity.ok(new ChatResponse(
                null, 
                request.getMessage(), 
                response, 
                java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("POST /chatbot/chat - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (user == null) {
                return ResponseEntity.status(401)
                    .body("Vui lòng đăng nhập để xem lịch sử chat");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessage> chatHistory = chatbotService.getChatHistory(user, pageable);
            
            return ResponseEntity.ok(chatHistory);

        } catch (Exception e) {
            logger.error("GET /chatbot/history - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @DeleteMapping("/history")
    public ResponseEntity<?> clearChatHistory(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(401)
                    .body("Vui lòng đăng nhập");
            }

            chatbotService.clearChatHistory(user);
            return ResponseEntity.ok("Đã xóa lịch sử chat");

        } catch (Exception e) {
            logger.error("DELETE /chatbot/history - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
