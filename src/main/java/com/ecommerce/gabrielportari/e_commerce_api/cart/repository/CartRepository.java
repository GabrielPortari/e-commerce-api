package com.ecommerce.gabrielportari.e_commerce_api.cart.repository;

import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionId(String sessionId);
}
