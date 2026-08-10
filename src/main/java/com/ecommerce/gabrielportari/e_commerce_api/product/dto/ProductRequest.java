package com.ecommerce.gabrielportari.e_commerce_api.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        String description,
        @NotNull(message = "Preço é obrigatório") @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
                BigDecimal price,
        @NotNull(message = "Estoque é obrigatório") @Min(value = 0, message = "Estoque não pode ser negativo")
                Integer stock,
        String imageUrl,
        @NotNull(message = "Categoria é obrigatória") Long categoryId,
        boolean onSale,
        @DecimalMin(value = "0.01", message = "Preço promocional deve ser maior que zero") BigDecimal discountPrice) {}
