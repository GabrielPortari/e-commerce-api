package com.ecommerce.gabrielportari.e_commerce_api.product.dto;

import com.ecommerce.gabrielportari.e_commerce_api.product.entity.ProductImage;

public record ProductImageResponse(Long id, String imageUrl, Integer displayOrder) {

    public static ProductImageResponse fromEntity(ProductImage image) {
        return new ProductImageResponse(image.getId(), image.getImageUrl(), image.getDisplayOrder());
    }
}
