package com.ecommerce.gabrielportari.e_commerce_api.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120, message = "Nome muito longo")
                String authorName,
        @NotNull(message = "Nota é obrigatória") @Min(value = 1, message = "Nota deve ser entre 1 e 5")
                @Max(value = 5, message = "Nota deve ser entre 1 e 5")
                Integer rating,
        String comment) {}
