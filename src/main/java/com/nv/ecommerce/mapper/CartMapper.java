package com.nv.ecommerce.mapper;

import com.nv.ecommerce.dto.response.CartItemResponseDto;
import com.nv.ecommerce.dto.response.CartResponseDto;
import com.nv.ecommerce.entity.Cart;
import com.nv.ecommerce.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    // Convert CartItem - DTO
    public static CartItemResponseDto toCartItemDto(CartItem item) {

        CartItemResponseDto dto = new CartItemResponseDto();

        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());

        BigDecimal total = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        dto.setTotal(total);

        return dto;
    }

    //  Convert Cart - DTO
    public static CartResponseDto toCartResponse(Cart cart) {

        List<CartItemResponseDto> items = cart.getItems()
                .stream()
                .map(CartMapper::toCartItemDto)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponseDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponseDto response = new CartResponseDto();
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }
}