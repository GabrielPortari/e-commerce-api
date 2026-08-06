package com.ecommerce.gabrielportari.e_commerce_api.cart.repository;

import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
