package com.gaubong.teddybearshop.controller;

import com.gaubong.teddybearshop.entity.Cart;
import com.gaubong.teddybearshop.entity.CartItem;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.security.JwtUtils;
import com.gaubong.teddybearshop.service.CartService;
import com.gaubong.teddybearshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtil;

    // DTO classes
    public static class AddToCartRequest {
        private Long productId;
        private Integer quantity;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class UpdateQuantityRequest {
        private Long productId;
        private Integer quantity;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // Lấy cart hiện tại (yêu cầu đăng nhập)
    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                // Trả về empty cart nếu chưa đăng nhập
                return ResponseEntity.ok(createEmptyCartResponse());
            }
            
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(createEmptyCartResponse());
            }
            
            Cart cart = cartService.getOrCreateCartForUser(userOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cart", cart);
            response.put("totalItems", cart.getTotalItems());
            response.put("totalPrice", cart.getTotalPrice());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy giỏ hàng: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Thêm sản phẩm vào cart (yêu cầu đăng nhập)
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpServletRequest httpRequest) {
        try {
            // Lấy token từ request
            String token = getTokenFromRequest(httpRequest);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
                return ResponseEntity.status(401).body(response);
            }
            
            // Lấy user từ token
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            
            // Lấy hoặc tạo cart cho user
            Cart cart = cartService.getOrCreateCartForUser(userOpt.get());
            
            // Thêm sản phẩm vào cart
            CartItem cartItem = cartService.addToCart(cart, request.getProductId(), request.getQuantity());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng");
            response.put("cart", cart);
            response.put("cartItem", cartItem);
            response.put("totalItems", cart.getTotalItems());
            response.put("totalPrice", cart.getTotalPrice());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Cập nhật quantity (yêu cầu đăng nhập)
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody UpdateQuantityRequest request, HttpServletRequest httpRequest) {
        try {
            String token = getTokenFromRequest(httpRequest);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            
            Cart cart = cartService.getOrCreateCartForUser(userOpt.get());
            CartItem cartItem = cartService.updateCartItemQuantity(cart, request.getProductId(), request.getQuantity());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật số lượng");
            response.put("cart", cart);
            response.put("cartItem", cartItem);
            response.put("totalItems", cart.getTotalItems());
            response.put("totalPrice", cart.getTotalPrice());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Xóa sản phẩm khỏi cart (yêu cầu đăng nhập)
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            
            Cart cart = cartService.getOrCreateCartForUser(userOpt.get());

            cartService.removeFromCart(cart, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            response.put("cart", cart);
            response.put("totalItems", cart.getTotalItems());
            response.put("totalPrice", cart.getTotalPrice());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Xóa tất cả items trong cart (yêu cầu đăng nhập)
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            
            if (token == null || !jwtUtil.validateJwtToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            Cart cart = cartService.getOrCreateCartForUser(userOpt.get());
            cartService.clearCart(cart);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa tất cả sản phẩm trong giỏ hàng");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Merge guest cart với user cart khi login
    @PostMapping("/merge")
    public ResponseEntity<?> mergeCart(@RequestParam String sessionId, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Cần đăng nhập");
                return ResponseEntity.badRequest().body(response);
            }

            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy user");
                return ResponseEntity.badRequest().body(response);
            }
            
            Cart mergedCart = cartService.mergeGuestCartWithUserCart(sessionId, userOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hợp nhất giỏ hàng");
            response.put("cart", mergedCart);
            response.put("totalItems", mergedCart.getTotalItems());
            response.put("totalPrice", mergedCart.getTotalPrice());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Helper methods
    private Cart getCurrentCart(HttpServletRequest request, String sessionId) {
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtUtil.validateJwtToken(token)) {
            // User đã đăng nhập
            String username = jwtUtil.getUserNameFromJwtToken(token);
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (userOpt.isPresent()) {
                return cartService.getOrCreateCartForUser(userOpt.get());
            }
        } else if (sessionId != null) {
            // Guest user
            Optional<Cart> cart = cartService.getCartBySessionId(sessionId);
            return cart.orElse(null);
        }
        
        return null;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Map<String, Object> createEmptyCartResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cart", null);
        response.put("totalItems", 0);
        response.put("totalPrice", 0.0);
        return response;
    }
}
