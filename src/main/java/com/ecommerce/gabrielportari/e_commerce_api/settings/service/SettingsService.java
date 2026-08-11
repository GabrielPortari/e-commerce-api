package com.ecommerce.gabrielportari.e_commerce_api.settings.service;

import com.ecommerce.gabrielportari.e_commerce_api.settings.dto.SettingsResponse;
import com.ecommerce.gabrielportari.e_commerce_api.settings.dto.UpdateSettingsRequest;
import com.ecommerce.gabrielportari.e_commerce_api.settings.entity.StoreSettings;
import com.ecommerce.gabrielportari.e_commerce_api.settings.repository.StoreSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final StoreSettingsRepository storeSettingsRepository;

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return SettingsResponse.fromEntity(findOrCreateSingleton());
    }

    @Transactional
    public SettingsResponse update(UpdateSettingsRequest request) {
        StoreSettings settings = findOrCreateSingleton();
        settings.setWhatsappNumber(request.whatsappNumber());
        return SettingsResponse.fromEntity(storeSettingsRepository.save(settings));
    }

    private StoreSettings findOrCreateSingleton() {
        return storeSettingsRepository
                .findById(StoreSettings.SINGLETON_ID)
                .orElseGet(() -> StoreSettings.builder()
                        .id(StoreSettings.SINGLETON_ID)
                        .build());
    }
}
