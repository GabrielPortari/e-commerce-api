package com.ecommerce.gabrielportari.e_commerce_api.order.repository;

import com.ecommerce.gabrielportari.e_commerce_api.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
