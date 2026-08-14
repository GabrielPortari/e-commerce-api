package com.ecommerce.gabrielportari.e_commerce_api.review.service;

import com.ecommerce.gabrielportari.e_commerce_api.exception.DuplicateReviewException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewPageResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewRequest;
import com.ecommerce.gabrielportari.e_commerce_api.review.dto.ReviewResponse;
import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import com.ecommerce.gabrielportari.e_commerce_api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ReviewPageResponse findByProduct(Long productId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), sortFor(sort));
        Page<ReviewResponse> reviews =
                reviewRepository.findByProductId(productId, pageable).map(ReviewResponse::fromEntity);
        return ReviewPageResponse.fromPage(reviews);
    }

    @Transactional
    public ReviewResponse create(Long productId, ReviewRequest request, String authorIp) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));
        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado: " + productId);
        }
        if (reviewRepository.existsByProductIdAndAuthorIp(productId, authorIp)) {
            throw new DuplicateReviewException("Você já avaliou este produto.");
        }

        Review review = Review.builder()
                .product(product)
                .authorName(request.authorName())
                .authorIp(authorIp)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        try {
            // saveAndFlush (não save) porque o insert só dispara de fato no flush —
            // com save() puro a violação do índice único só apareceria no commit da
            // transação, fora deste try/catch.
            return ReviewResponse.fromEntity(reviewRepository.saveAndFlush(review));
        } catch (DataIntegrityViolationException ex) {
            // Corrida entre duas requisições simultâneas do mesmo IP passando pelo
            // existsBy acima antes de qualquer uma salvar — o índice único
            // (ux_reviews_product_author_ip) barra a segunda no banco.
            throw new DuplicateReviewException("Você já avaliou este produto.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Avaliação não encontrada: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private int clampSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private Sort sortFor(String sort) {
        return switch (sort == null ? "recent" : sort) {
            case "highest" -> Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("createdAt"));
            case "lowest" -> Sort.by(Sort.Order.asc("rating"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }
}
