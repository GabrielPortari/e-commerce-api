package com.ecommerce.gabrielportari.e_commerce_api.review.controller;

import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewRequest;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    public List<ReviewResponse> findByProduct(@PathVariable Long productId) {
        return reviewService.findByProduct(productId);
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> create(
            @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(productId, request));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
