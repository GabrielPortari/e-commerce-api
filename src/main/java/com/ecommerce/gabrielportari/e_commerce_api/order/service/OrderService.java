package com.ecommerce.gabrielportari.e_commerce_api.order.service;

import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.Cart;
import com.ecommerce.gabrielportari.e_commerce_api.cart.entity.CartItem;
import com.ecommerce.gabrielportari.e_commerce_api.cart.repository.CartRepository;
import com.ecommerce.gabrielportari.e_commerce_api.exception.BusinessException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.InsufficientStockException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.order.dto.CreateOrderRequest;
import com.ecommerce.gabrielportari.e_commerce_api.order.dto.OrderResponse;
import com.ecommerce.gabrielportari.e_commerce_api.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.gabrielportari.e_commerce_api.order.entity.Order;
import com.ecommerce.gabrielportari.e_commerce_api.order.entity.OrderItem;
import com.ecommerce.gabrielportari.e_commerce_api.order.repository.OrderRepository;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse checkout(String sessionId, CreateOrderRequest request) {
        Cart cart = cartRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException("Carrinho não encontrado"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Carrinho vazio");
        }

        Order order = Order.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerAddress(request.customerAddress())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Estoque insuficiente para o produto " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal unitPrice = product.getEffectivePrice();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            order.getItems().add(orderItem);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return OrderResponse.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = findEntityById(id);
        order.setStatus(request.status());
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    private Order findEntityById(Long id) {
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }
}
