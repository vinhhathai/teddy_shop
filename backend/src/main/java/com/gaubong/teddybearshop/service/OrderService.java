package com.gaubong.teddybearshop.service;

import com.gaubong.teddybearshop.dto.CreateOrderRequest;
import com.gaubong.teddybearshop.entity.Order;
import com.gaubong.teddybearshop.entity.OrderItem;
import com.gaubong.teddybearshop.entity.Product;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.repository.OrderRepository;
import com.gaubong.teddybearshop.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Page<Order> getOrdersByUser(User user, Pageable pageable) {
        return orderRepository.findByUser(user, pageable);
    }

    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Page<Order> getOrdersWithFilters(Long userId, Order.OrderStatus status, 
                                           LocalDateTime startDate, LocalDateTime endDate, 
                                           Pageable pageable) {
        return orderRepository.findOrdersWithFilters(userId, status, startDate, endDate, pageable);
    }

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> orderItems, String shippingAddress, String phoneNumber, String notes) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Validate stock availability
        for (OrderItem item : orderItems) {
            Product product = productService.getProductById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProduct().getId()));
            
            if (!productService.isProductAvailable(product.getId(), item.getQuantity())) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        // Create order
        Order order = new Order(user, totalAmount, shippingAddress, phoneNumber);
        order.setNotes(notes);
        order = orderRepository.save(order);

        // Create order items and update stock
        for (OrderItem item : orderItems) {
            Product product = productService.getProductById(item.getProduct().getId()).get();
            item.setOrder(order);
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            item.calculateTotalPrice();
            
            orderItemRepository.save(item);
            productService.decreaseStock(product.getId(), item.getQuantity());
        }

        return order;
    }

    @Transactional
    public Order createOrder(User user, CreateOrderRequest orderRequest) {
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            Product product = productService.getProductById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);
        }
        
        return createOrder(user.getId(), orderItems, orderRequest.getShippingAddress(), 
                          orderRequest.getPhoneNumber(), orderRequest.getNotes());
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setShippingAddress(orderDetails.getShippingAddress());
        order.setPhoneNumber(orderDetails.getPhoneNumber());
        order.setNotes(orderDetails.getNotes());
        order.setStatus(orderDetails.getStatus());

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        orderRepository.delete(order);
    }

    public BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> {
                    Product product = productService.getProductById(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmountByUserId(Long userId) {
        BigDecimal total = orderRepository.getTotalAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Long countOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public Map<Order.OrderStatus, Long> getOrderCountByStatus() {
        Map<Order.OrderStatus, Long> statusCounts = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            statusCounts.put(status, countOrdersByStatus(status));
        }
        return statusCounts;
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}