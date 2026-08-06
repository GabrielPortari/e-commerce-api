package com.ecommerce.gabrielportari.e_commerce_api.cart.dto;

import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.Cart;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, String sessionId, List<CartItemResponse> items, BigDecimal total) {

    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> items =
                cart.getItems().stream().map(CartItemResponse::fromEntity).toList();

        BigDecimal total =
                items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getSessionId(), items, total);
    }
}
