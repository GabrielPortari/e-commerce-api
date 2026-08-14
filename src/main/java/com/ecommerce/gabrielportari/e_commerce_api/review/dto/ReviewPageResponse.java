package com.ecommerce.gabrielportari.e_commerce_api.review.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record ReviewPageResponse(
        List<ReviewResponse> content, int page, int size, long totalElements, int totalPages) {

    public static ReviewPageResponse fromPage(Page<ReviewResponse> page) {
        return new ReviewPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
