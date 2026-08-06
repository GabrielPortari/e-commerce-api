package com.ecommerce.gabrielportari.e_commerce_api.order.dto;

import com.ecommerce.gabrielportari.e_commerce_api.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull(message = "Status é obrigatório") OrderStatus status) {}
