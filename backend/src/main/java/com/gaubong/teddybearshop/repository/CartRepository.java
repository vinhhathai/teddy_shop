package com.gaubong.teddybearshop.repository;

import com.gaubong.teddybearshop.entity.Cart;
import com.gaubong.teddybearshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Tìm cart theo user
    Optional<Cart> findByUser(User user);
    
    // Tìm cart theo user ID
    Optional<Cart> findByUserId(Long userId);
    
    // Tìm cart theo session ID (cho guest users)
    Optional<Cart> findBySessionId(String sessionId);
    
    // Tìm tất cả cart đã hết hạn
    @Query("SELECT c FROM Cart c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :currentTime")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);
    
    // Tìm cart của guest users đã hết hạn
    @Query("SELECT c FROM Cart c WHERE c.sessionId IS NOT NULL AND c.user IS NULL AND c.expiresAt < :currentTime")
    List<Cart> findExpiredGuestCarts(@Param("currentTime") LocalDateTime currentTime);
    
    // Xóa cart đã hết hạn
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    // Đếm số lượng items trong cart của user
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.user.id = :userId")
    Integer countItemsByUserId(@Param("userId") Long userId);
    
    // Đếm số lượng items trong cart theo session ID
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.sessionId = :sessionId")
    Integer countItemsBySessionId(@Param("sessionId") String sessionId);
    
    // Tính tổng giá trị cart của user
    @Query("SELECT COALESCE(SUM(ci.priceAtTime * ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.user.id = :userId")
    Double calculateTotalByUserId(@Param("userId") Long userId);
    
    // Tính tổng giá trị cart theo session ID
    @Query("SELECT COALESCE(SUM(ci.priceAtTime * ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.sessionId = :sessionId")
    Double calculateTotalBySessionId(@Param("sessionId") String sessionId);
    
    // Kiểm tra xem user có cart không
    boolean existsByUser(User user);
    
    // Kiểm tra xem session có cart không
    boolean existsBySessionId(String sessionId);
}