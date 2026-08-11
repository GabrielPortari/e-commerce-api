package com.ecommerce.gabrielportari.e_commerce_api.review.dto;

import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id, String authorName, Integer rating, String comment, LocalDateTime createdAt) {

    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getAuthorName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
