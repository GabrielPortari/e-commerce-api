package com.ecommerce.gabrielportari.e_commerce_api.review.service;

import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewRequest;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import com.ecommerce.gabrielportari.e_commerce_api.review.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> findByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ReviewResponse create(Long productId, ReviewRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        Review review = Review.builder()
                .product(product)
                .authorName(request.authorName())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return ReviewResponse.fromEntity(reviewRepository.save(review));
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Avaliação não encontrada: " + id);
        }
        reviewRepository.deleteById(id);
    }
}
