package com.nv.ecommerce.service.impl;

import com.nv.ecommerce.dto.request.OrderRequestDto;
import com.nv.ecommerce.dto.response.OrderResponseDto;
import com.nv.ecommerce.entity.*;
import com.nv.ecommerce.enums.OrderStatus;
import com.nv.ecommerce.exception.ResourceNotFoundException;
import com.nv.ecommerce.mapper.OrderMapper;
import com.nv.ecommerce.repository.*;
import com.nv.ecommerce.service.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // Get Logged-in User
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    @Transactional
    @Override
    public OrderResponseDto placeOrder(OrderRequestDto request) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        //1. Validate stock BEFORE creating order
        cart.getItems().forEach(cartItem -> {

            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        });

        // 2. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(request.getShippingAddress());

        // 3. Convert CartItems - > OrderItems
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {

            Product product = cartItem.getProduct();

            // Reduce stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());

            return item;

        }).toList();

        order.setItems(orderItems);

        // 4. Calculate total
        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        // 5. Save order
        Order savedOrder = orderRepository.save(order);

        // 6. Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderMapper.toResponse(savedOrder);
    }

    // GET USER ORDERS
    @Override
    public List<OrderResponseDto> getMyOrders() {

        User user = getCurrentUser();

        return orderRepository.findByUser(user)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
    
    
    @Transactional
    @Override
    public void cancelOrder(Long orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Ownership check
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        //  Prevent cancelling paid orders
        if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Cannot cancel a paid order");
        }

        // 1. Restore stock
        order.getItems().forEach(item -> {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        });

        // 2. Update order status
        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
}