package com.ecommerce.gabrielportari.e_commerce_api.review.repository;

import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
}
