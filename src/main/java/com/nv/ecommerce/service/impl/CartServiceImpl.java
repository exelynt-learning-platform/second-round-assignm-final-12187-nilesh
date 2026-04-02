package com.nv.ecommerce.service.impl;

import com.nv.ecommerce.dto.response.CartResponseDto;
import com.nv.ecommerce.entity.*;
import com.nv.ecommerce.exception.InsufficientStockException;
import com.nv.ecommerce.exception.ResourceNotFoundException;
import com.nv.ecommerce.mapper.CartMapper;
import com.nv.ecommerce.repository.*;
import com.nv.ecommerce.service.CartService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Get Logged-in User
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Get or Create Cart
    private Cart getOrCreateCart(User user) {

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(cart);
                });
    }

    // ADD TO CART
    @Override
    @Transactional
    public void addToCart(Long productId, int quantity) {

        // 1. Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // 2. Get current user & cart
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        // 3. Fetch product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // 4. Check existing cart item
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {

            int updatedQuantity = cartItem.getQuantity() + quantity;

            // STOCK VALIDATION 
            if (product.getStockQuantity() < updatedQuantity) {
                throw new InsufficientStockException(
                        "Only " + product.getStockQuantity() + " items available in stock"
                );
            }

            cartItem.setQuantity(updatedQuantity);

        } else {

            // STOCK VALIDATION (IMPORTANT)
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException(
                        "Only " + product.getStockQuantity() + " items available in stock"
                );
            }

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();

            cart.getItems().add(cartItem);
        }

        // 5. Save
        cartItemRepository.save(cartItem);
    }

    // GET CART (USING MAPPER)
    @Override
    public CartResponseDto getCart() {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        return CartMapper.toCartResponse(cart);
    }

    // REMOVE ITEM
    @Override
    @Transactional
    public void removeFromCart(Long productId) {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cart.getItems().remove(cartItem);

        cartItemRepository.delete(cartItem);
    }

    // CLEAR CART
    @Override
    @Transactional
    public void clearCart() {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cart.getItems().clear();

        cartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void updateCart(Long productId, int quantity) {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity == 0) {
            // Remove item
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            // Update quantity
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }
}