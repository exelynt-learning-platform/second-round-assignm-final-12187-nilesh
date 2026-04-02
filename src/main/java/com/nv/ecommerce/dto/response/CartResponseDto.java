package com.nv.ecommerce.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponseDto {

    private List<CartItemResponseDto> items;
    private BigDecimal totalAmount;
}