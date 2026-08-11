package com.ecommerce.gabrielportari.e_commerce_api.settings.controller;

import com.ecommerce.gabrielportari.e_commerce_api.settings.dto.SettingsResponse;
import com.ecommerce.gabrielportari.e_commerce_api.settings.dto.UpdateSettingsRequest;
import com.ecommerce.gabrielportari.e_commerce_api.settings.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public SettingsResponse get() {
        return settingsService.get();
    }

    @PutMapping
    public SettingsResponse update(@Valid @RequestBody UpdateSettingsRequest request) {
        return settingsService.update(request);
    }
}
