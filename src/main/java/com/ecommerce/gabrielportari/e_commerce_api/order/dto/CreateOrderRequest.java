package com.ecommerce.gabrielportari.e_commerce_api.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "Nome é obrigatório") String customerName,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String customerEmail,
        @NotBlank(message = "Endereço é obrigatório") String customerAddress) {}
