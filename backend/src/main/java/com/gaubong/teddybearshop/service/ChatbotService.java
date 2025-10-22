package com.gaubong.teddybearshop.service;

import com.gaubong.teddybearshop.entity.ChatMessage;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.repository.ChatMessageRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private Integer maxTokens;

    @Value("${openai.temperature:0.7}")
    private Double temperature;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private static final String SYSTEM_PROMPT = 
        "Bạn là trợ lý ảo của Shop Gấu Bông - một cửa hàng chuyên bán gấu bông đáng yêu. " +
        "Nhiệm vụ của bạn là hỗ trợ khách hàng về: " +
        "1. Thông tin sản phẩm gấu bông (giá, kích thước, chất liệu) " +
        "2. Hướng dẫn đặt hàng và thanh toán " +
        "3. Chính sách giao hàng và đổi trả " +
        "4. Tư vấn chọn quà tặng phù hợp " +
        "Hãy trả lời thân thiện, nhiệt tình và chuyên nghiệp bằng tiếng Việt.";

    public String getChatResponse(String userMessage, User user) {
        try {
            // Kiểm tra API key
            if (apiKey == null || apiKey.equals("YOUR_OPENAI_API_KEY_HERE")) {
                logger.warn("OpenAI API key chưa được cấu hình");
                return "Xin lỗi, tính năng chatbot chưa được kích hoạt. Vui lòng liên hệ admin để được hỗ trợ.";
            }

            // Tạo OpenAI service với timeout
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));

            // Tạo danh sách messages cho GPT
            List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
            
            // System message
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", SYSTEM_PROMPT));
            
            // Nếu có user, lấy lịch sử chat gần đây để context
            if (user != null) {
                List<ChatMessage> recentChats = chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
                
                // Thêm context từ lịch sử (đảo ngược để đúng thứ tự thời gian)
                for (int i = recentChats.size() - 1; i >= 0; i--) {
                    ChatMessage chat = recentChats.get(i);
                    messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", chat.getUserMessage()));
                    messages.add(new com.theokanning.openai.completion.chat.ChatMessage("assistant", chat.getBotResponse()));
                }
            }
            
            // Thêm tin nhắn mới
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", userMessage));

            // Tạo request
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .build();

            // Gọi API
            String response = service.createChatCompletion(completionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            // Lưu vào database nếu user đã đăng nhập
            if (user != null) {
                ChatMessage chatMessage = new ChatMessage(user, userMessage, response);
                chatMessageRepository.save(chatMessage);
                logger.info("Chat response generated and saved for user: {}", user.getUsername());
            } else {
                logger.info("Chat response generated for anonymous user");
            }

            return response;

        } catch (Exception e) {
            logger.error("Error calling OpenAI API: {}", e.getMessage(), e);
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ hotline để được hỗ trợ trực tiếp.";
        }
    }

    public Page<ChatMessage> getChatHistory(User user, Pageable pageable) {
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public void clearChatHistory(User user) {
        List<ChatMessage> userChats = chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        chatMessageRepository.deleteAll(userChats);
    }
}
