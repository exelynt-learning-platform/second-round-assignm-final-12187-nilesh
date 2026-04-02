package com.nv.ecommerce.mapper;

import com.nv.ecommerce.dto.response.OrderItemResponseDto;
import com.nv.ecommerce.dto.response.OrderResponseDto;
import com.nv.ecommerce.entity.Order;
import com.nv.ecommerce.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public class OrderMapper {

    public static OrderItemResponseDto toItemDto(OrderItem item) {

        OrderItemResponseDto dto = new OrderItemResponseDto();

        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());

        BigDecimal total = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        dto.setTotal(total);

        return dto;
    }

    public static OrderResponseDto toResponse(Order order) {

        List<OrderItemResponseDto> items = order.getItems()
                .stream()
                .map(OrderMapper::toItemDto)
                .toList();

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getId());
        dto.setItems(items);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }
}