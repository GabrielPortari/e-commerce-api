package com.ecommerce.gabrielportari.e_commerce_api.cart.controller;

import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.CartResponse;
import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.UpdateCartItemRequest;
import com.ecommerce.gabrielportari.e_commerce_api.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@RequestHeader(SESSION_HEADER) String sessionId) {
        return cartService.getOrCreateCart(sessionId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader(SESSION_HEADER) String sessionId, @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(sessionId, request));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @RequestHeader(SESSION_HEADER) String sessionId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItemQuantity(sessionId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@RequestHeader(SESSION_HEADER) String sessionId, @PathVariable Long itemId) {
        return cartService.removeItem(sessionId, itemId);
    }

    @DeleteMapping
    public CartResponse clearCart(@RequestHeader(SESSION_HEADER) String sessionId) {
        return cartService.clearCart(sessionId);
    }
}
