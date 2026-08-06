package com.ecommerce.gabrielportari.e_commerce_api.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank(message = "Nome é obrigatório") String name) {}
