package com.gaubong.teddybearshop.service;

import com.gaubong.teddybearshop.entity.Cart;
import com.gaubong.teddybearshop.entity.CartItem;
import com.gaubong.teddybearshop.entity.Product;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.repository.CartRepository;
import com.gaubong.teddybearshop.repository.CartItemRepository;
import com.gaubong.teddybearshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Lấy hoặc tạo cart cho user
    public Cart getOrCreateCartForUser(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        Cart newCart = new Cart(user);
        return cartRepository.save(newCart);
    }

    // Lấy hoặc tạo cart cho guest user
    public Cart getOrCreateCartForGuest(String sessionId) {
        Optional<Cart> existingCart = cartRepository.findBySessionId(sessionId);
        if (existingCart.isPresent() && !existingCart.get().isExpired()) {
            return existingCart.get();
        }
        
        Cart newCart = new Cart(sessionId);
        return cartRepository.save(newCart);
    }

    // Thêm sản phẩm vào cart
    public CartItem addToCart(Cart cart, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Kiểm tra stock - handle null case
        Integer currentStock = product.getStock();
        if (currentStock == null || currentStock < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // Cập nhật quantity nếu item đã tồn tại
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            // Kiểm tra stock với null check
            if (currentStock == null || currentStock < newQuantity) {
                throw new RuntimeException("Không đủ hàng trong kho");
            }
            
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            // Tạo item mới
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.addCartItem(newItem);
            return cartItemRepository.save(newItem);
        }
    }

    // Cập nhật quantity của item trong cart
    public CartItem updateCartItemQuantity(Cart cart, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (quantity <= 0) {
            removeFromCart(cart, productId);
            return null;
        }

        // Kiểm tra stock với null check
        Integer currentStock = product.getStock();
        if (currentStock == null || currentStock < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    // Xóa sản phẩm khỏi cart
    public void removeFromCart(Cart cart, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    // Xóa tất cả items trong cart
    public void clearCart(Cart cart) {
        cartItemRepository.deleteByCart(cart);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    // Lấy cart theo user ID
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    // Lấy cart theo session ID
    public Optional<Cart> getCartBySessionId(String sessionId) {
        return cartRepository.findBySessionId(sessionId);
    }

    // Chuyển đổi guest cart thành user cart khi login
    public Cart mergeGuestCartWithUserCart(String sessionId, User user) {
        Optional<Cart> guestCart = cartRepository.findBySessionId(sessionId);
        Optional<Cart> userCart = cartRepository.findByUser(user);

        if (!guestCart.isPresent()) {
            return getOrCreateCartForUser(user);
        }

        if (!userCart.isPresent()) {
            // Chuyển guest cart thành user cart
            Cart cart = guestCart.get();
            cart.setUser(user);
            cart.setSessionId(null);
            cart.setExpiresAt(null);
            return cartRepository.save(cart);
        }

        // Merge guest cart vào user cart
        Cart targetCart = userCart.get();
        Cart sourceCart = guestCart.get();

        for (CartItem guestItem : sourceCart.getCartItems()) {
            Optional<CartItem> existingItem = cartItemRepository
                    .findByCartAndProduct(targetCart, guestItem.getProduct());

            if (existingItem.isPresent()) {
                // Cộng dồn quantity
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + guestItem.getQuantity();
                
                // Kiểm tra stock với null check
                Integer productStock = guestItem.getProduct().getStock();
                if (productStock != null && productStock >= newQuantity) {
                    item.setQuantity(newQuantity);
                    cartItemRepository.save(item);
                }
            } else {
                // Thêm item mới nếu có stock
                Integer productStock = guestItem.getProduct().getStock();
                if (productStock != null && productStock >= guestItem.getQuantity()) {
                    CartItem newItem = new CartItem(targetCart, guestItem.getProduct(), guestItem.getQuantity());
                    targetCart.addCartItem(newItem);
                    cartItemRepository.save(newItem);
                }
            }
        }

        // Xóa guest cart
        cartRepository.delete(sourceCart);
        return cartRepository.save(targetCart);
    }

    // Tính tổng số items trong cart
    public int getTotalItems(Cart cart) {
        return cart.getTotalItems();
    }

    // Tính tổng giá trị cart
    public double getTotalPrice(Cart cart) {
        return cart.getTotalPrice();
    }

    // Kiểm tra xem product có trong cart không
    public boolean isProductInCart(Cart cart, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        
        return cartItemRepository.existsByCartAndProduct(cart, product);
    }

    // Lấy quantity của product trong cart
    public int getProductQuantityInCart(Cart cart, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0;
        
        Optional<CartItem> cartItem = cartItemRepository.findByCartAndProduct(cart, product);
        return cartItem.map(CartItem::getQuantity).orElse(0);
    }

    // Scheduled task để xóa expired guest carts (chạy hàng ngày lúc 2:00 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findExpiredGuestCarts(now);
        
        for (Cart cart : expiredCarts) {
            cartItemRepository.deleteByCart(cart);
            cartRepository.delete(cart);
        }
    }
}