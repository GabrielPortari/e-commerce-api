package com.ecommerce.gabrielportari.e_commerce_api.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateSettingsRequest(
        @NotBlank(message = "Número do WhatsApp é obrigatório")
                @Pattern(
                        regexp = "^[0-9]{10,15}$",
                        message = "Número do WhatsApp deve conter apenas dígitos, com DDI e DDD (ex: 5511999999999)")
                String whatsappNumber) {}
