package com.gaubong.teddybearshop.repository;

import com.gaubong.teddybearshop.entity.Cart;
import com.gaubong.teddybearshop.entity.CartItem;
import com.gaubong.teddybearshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Tìm tất cả cart items theo cart
    List<CartItem> findByCart(Cart cart);
    
    // Tìm cart item theo cart và product
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // Tìm cart item theo cart ID và product ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    
    // Xóa tất cả cart items theo cart
    void deleteByCart(Cart cart);
    
    // Xóa cart item theo cart và product
    void deleteByCartAndProduct(Cart cart, Product product);
    
    // Đếm số lượng items trong cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);
    
    // Tính tổng quantity trong cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);
    
    // Tính tổng giá trị cart
    @Query("SELECT COALESCE(SUM(ci.priceAtTime * ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double sumTotalByCartId(@Param("cartId") Long cartId);
    
    // Kiểm tra xem product có trong cart không
    boolean existsByCartAndProduct(Cart cart, Product product);
    
    // Tìm cart items theo product (để kiểm tra khi xóa product)
    List<CartItem> findByProduct(Product product);
}