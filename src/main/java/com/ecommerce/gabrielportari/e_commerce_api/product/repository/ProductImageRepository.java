package com.ecommerce.gabrielportari.e_commerce_api.product.repository;

import com.ecommerce.gabrielportari.e_commerce_api.product.entity.ProductImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    long countByProductId(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);
}
