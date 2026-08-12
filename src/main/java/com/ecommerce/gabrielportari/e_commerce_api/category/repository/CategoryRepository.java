package com.ecommerce.gabrielportari.e_commerce_api.category.repository;

import com.ecommerce.gabrielportari.e_commerce_api.category.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByIsDefaultTrue();
}
