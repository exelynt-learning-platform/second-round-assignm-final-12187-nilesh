package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.OrderRequestDto;
import com.nv.ecommerce.dto.response.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder(OrderRequestDto request);

    List<OrderResponseDto> getMyOrders();
    
    void cancelOrder(Long orderId);
}