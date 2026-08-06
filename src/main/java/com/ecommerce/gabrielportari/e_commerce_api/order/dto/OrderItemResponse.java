package com.ecommerce.gabrielportari.e_commerce_api.order.dto;

import com.ecommerce.gabrielportari.e_commerce_api.order.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id, Long productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {

    public static OrderItemResponse fromEntity(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal);
    }
}
