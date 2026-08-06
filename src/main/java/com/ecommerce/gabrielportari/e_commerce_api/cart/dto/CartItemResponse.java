package com.ecommerce.gabrielportari.e_commerce_api.cart.dto;

import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal) {

    public static CartItemResponse fromEntity(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                unitPrice,
                item.getQuantity(),
                subtotal);
    }
}
