package com.ecommerce.gabrielportari.e_commerce_api.product.dto;

import com.ecommerce.gabrielportari.e_commerce_api.category.dto.CategoryResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        CategoryResponse category,
        Boolean active,
        Boolean onSale,
        BigDecimal discountPrice,
        Boolean featured,
        LocalDateTime createdAt) {

    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                CategoryResponse.fromEntity(product.getCategory()),
                product.getActive(),
                product.getOnSale(),
                product.getDiscountPrice(),
                product.getFeatured(),
                product.getCreatedAt());
    }
}
