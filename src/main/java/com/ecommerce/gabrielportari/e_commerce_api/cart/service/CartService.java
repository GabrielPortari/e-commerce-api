package com.ecommerce.gabrielportari.e_commerce_api.cart.service;

import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.AddCartItemRequest;
import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.CartResponse;
import com.ecommerce.gabrielportari.e_commerce_api.cart.dto.UpdateCartItemRequest;
import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.Cart;
import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.CartItem;
import com.ecommerce.gabrielportari.e_commerce_api.cart.repository.CartItemRepository;
import com.ecommerce.gabrielportari.e_commerce_api.cart.repository.CartRepository;
import com.ecommerce.gabrielportari.e_commerce_api.exception.InsufficientStockException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponse getOrCreateCart(String sessionId) {
        return CartResponse.fromEntity(findOrCreateCart(sessionId));
    }

    @Transactional
    public CartResponse addItem(String sessionId, AddCartItemRequest request) {
        Cart cart = findOrCreateCart(sessionId);
        Product product = findProductById(request.productId());

        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        int desiredQuantity =
                request.quantity() + (existingItem != null ? existingItem.getQuantity() : 0);
        validateStock(product, desiredQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(desiredQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = CartItem.builder().cart(cart).product(product).quantity(request.quantity()).build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(String sessionId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = findOrCreateCart(sessionId);
        CartItem item = findItemInCart(cart, itemId);

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse removeItem(String sessionId, Long itemId) {
        Cart cart = findOrCreateCart(sessionId);
        CartItem item = findItemInCart(cart, itemId);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse clearCart(String sessionId) {
        Cart cart = findOrCreateCart(sessionId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return CartResponse.fromEntity(cart);
    }

    private Cart findOrCreateCart(String sessionId) {
        return cartRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> cartRepository.save(Cart.builder().sessionId(sessionId).build()));
    }

    private CartItem findItemInCart(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado no carrinho: " + itemId));
    }

    private Product findProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private void validateStock(Product product, int desiredQuantity) {
        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado: " + product.getId());
        }
        if (product.getStock() < desiredQuantity) {
            throw new InsufficientStockException("Estoque insuficiente para o produto " + product.getName());
        }
    }
}
