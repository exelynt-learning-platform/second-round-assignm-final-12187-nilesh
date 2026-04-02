package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.response.CartResponseDto;

public interface CartService {

    void addToCart(Long productId, int quantity);

    CartResponseDto getCart();

    void removeFromCart(Long productId);

    void clearCart();
    
    void updateCart(Long productId, int quantity);
}