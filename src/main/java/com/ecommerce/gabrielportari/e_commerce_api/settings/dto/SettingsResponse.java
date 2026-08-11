package com.ecommerce.gabrielportari.e_commerce_api.settings.dto;

import com.ecommerce.gabrielportari.e_commerce_api.settings.entity.StoreSettings;

public record SettingsResponse(String whatsappNumber) {

    public static SettingsResponse fromEntity(StoreSettings settings) {
        return new SettingsResponse(settings.getWhatsappNumber());
    }
}
