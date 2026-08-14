package com.ecommerce.gabrielportari.e_commerce_api.review.repository;

import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    boolean existsByProductIdAndAuthorIp(Long productId, String authorIp);
}
