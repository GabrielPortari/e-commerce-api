package com.ecommerce.gabrielportari.e_commerce_api.product.dto;

import com.ecommerce.gabrielportari.e_commerce_api.category.dto.CategoryResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        List<ProductImageResponse> images,
        CategoryResponse category,
        Boolean active,
        Boolean onSale,
        BigDecimal discountPrice,
        Boolean featured,
        Double averageRating,
        Integer reviewCount,
        LocalDateTime createdAt) {

    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getImages().stream().map(ProductImageResponse::fromEntity).toList(),
                CategoryResponse.fromEntity(product.getCategory()),
                product.getActive(),
                product.getOnSale(),
                product.getDiscountPrice(),
                product.getFeatured(),
                product.getAverageRating(),
                product.getReviewCount(),
                product.getCreatedAt());
    }
}
