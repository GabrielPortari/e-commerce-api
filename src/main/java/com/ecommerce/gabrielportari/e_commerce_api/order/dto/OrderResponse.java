package com.ecommerce.gabrielportari.e_commerce_api.order.dto;

import com.ecommerce.gabrielportari.e_commerce_api.order.entity.Order;
import com.ecommerce.gabrielportari.e_commerce_api.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        String customerAddress,
        OrderStatus status,
        BigDecimal total,
        List<OrderItemResponse> items,
        LocalDateTime createdAt) {

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> items =
                order.getItems().stream().map(OrderItemResponse::fromEntity).toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerAddress(),
                order.getStatus(),
                order.getTotal(),
                items,
                order.getCreatedAt());
    }
}
