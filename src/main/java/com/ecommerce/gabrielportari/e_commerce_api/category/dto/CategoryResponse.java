package com.ecommerce.gabrielportari.e_commerce_api.category.dto;

import com.ecommerce.gabrielportari.e_commerce_api.category.entity.Category;

public record CategoryResponse(Long id, String name) {

    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
