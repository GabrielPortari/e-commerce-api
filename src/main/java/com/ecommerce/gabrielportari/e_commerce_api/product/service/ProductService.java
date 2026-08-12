package com.ecommerce.gabrielportari.e_commerce_api.product.service;

import com.ecommerce.gabrielportari.e_commerce_api.category.entity.Category;
import com.ecommerce.gabrielportari.e_commerce_api.category.repository.CategoryRepository;
import com.ecommerce.gabrielportari.e_commerce_api.exception.BusinessException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ProductRequest;
import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ProductResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductSpecifications;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllActive(Long categoryId, String name, Boolean onSale, Boolean featured) {
        Specification<Product> spec = ProductSpecifications.active(true);

        if (categoryId != null) {
            spec = spec.and(ProductSpecifications.categoryIdEquals(categoryId));
        }
        if (name != null && !name.isBlank()) {
            spec = spec.and(ProductSpecifications.nameContains(name));
        }
        if (onSale != null) {
            spec = spec.and(ProductSpecifications.onSaleEquals(onSale));
        }
        if (featured != null) {
            spec = spec.and(ProductSpecifications.featuredEquals(featured));
        }

        return productRepository.findAll(spec).stream().map(ProductResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllForAdmin() {
        return productRepository.findAll().stream().map(ProductResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveById(Long id) {
        Product product = findEntityById(id);
        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado: " + id);
        }
        return ProductResponse.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveBySlug(String slug) {
        Product product = productRepository
                .findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + slug));
        if (!product.getActive()) {
            throw new ResourceNotFoundException("Produto não encontrado: " + slug);
        }
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = findCategoryById(request.categoryId());
        validatePromotion(request);

        Product product = Product.builder()
                .name(request.name())
                .slug(generateUniqueSlug(request.name()))
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .category(category)
                .active(true)
                .onSale(request.onSale())
                .discountPrice(resolveDiscountPrice(request))
                .featured(request.featured())
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        Category category = findCategoryById(request.categoryId());
        validatePromotion(request);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);
        product.setOnSale(request.onSale());
        product.setDiscountPrice(resolveDiscountPrice(request));
        product.setFeatured(request.featured());

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    private BigDecimal resolveDiscountPrice(ProductRequest request) {
        return request.onSale() ? request.discountPrice() : null;
    }

    private void validatePromotion(ProductRequest request) {
        if (!request.onSale()) {
            return;
        }
        if (request.discountPrice() == null) {
            throw new BusinessException("Preço promocional é obrigatório para produtos em promoção");
        }
        if (request.discountPrice().compareTo(request.price()) >= 0) {
            throw new BusinessException("Preço promocional deve ser menor que o preço original");
        }
    }

    @Transactional
    public void softDelete(Long id) {
        Product product = findEntityById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse reactivate(Long id) {
        Product product = findEntityById(id);
        product.setActive(true);
        return ProductResponse.fromEntity(productRepository.save(product));
    }

    private String generateUniqueSlug(String name) {
        String base = slugify(name);
        String slug = base;
        int suffix = 2;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String slugify(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return slug.isBlank() ? "produto" : slug;
    }

    private Product findEntityById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }
}
