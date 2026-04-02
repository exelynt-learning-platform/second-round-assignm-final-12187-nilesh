package com.nv.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDto {

    private Long paymentId;

    private Long orderId;

    private BigDecimal amount;

    private String currency;

    private String status; // CREATED / SUCCESS / FAILED

    private String paymentMethod; // UPI / CARD / NETBANKING

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private LocalDateTime createdAt;
}