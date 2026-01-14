package com.cymelle.app.orders.dto;

import com.cymelle.app.orders.Order;
import com.cymelle.app.orders.OrderStatus;
import com.cymelle.app.orders.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long customerId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalCost;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalCost(order.getTotalCost())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(OrderItemResponse::from).toList())
                .build();
    }
}
