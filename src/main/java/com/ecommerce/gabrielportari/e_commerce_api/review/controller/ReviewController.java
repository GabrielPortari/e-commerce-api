package com.ecommerce.gabrielportari.e_commerce_api.review.controller;

import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewPageResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewRequest;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    public ReviewPageResponse findByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sort) {
        return reviewService.findByProduct(productId, page, size, sort);
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> create(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest httpRequest) {
        ReviewResponse response =
                reviewService.create(productId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
