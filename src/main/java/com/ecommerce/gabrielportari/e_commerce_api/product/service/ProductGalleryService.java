package com.ecommerce.gabrielportari.e_commerce_api.product.service;

import com.ecommerce.gabrielportari.e_commerce_api.exception.BusinessException;
import com.ecommerce.gabrielportari.e_commerce_api.exception.ResourceNotFoundException;
import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ProductImageResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.ProductImage;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductImageRepository;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductGalleryService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    private static final int MAX_IMAGES_PER_PRODUCT = 3;

    @Transactional
    public List<ProductImageResponse> addImage(Long productId, MultipartFile file) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        if (existingImages.size() >= MAX_IMAGES_PER_PRODUCT) {
            throw new BusinessException("Limite de " + MAX_IMAGES_PER_PRODUCT + " imagens por produto atingido");
        }

        String imageUrl = fileStorageService.store(file);
        int nextOrder = existingImages.stream().mapToInt(ProductImage::getDisplayOrder).max().orElse(-1) + 1;

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .displayOrder(nextOrder)
                .build();
        productImageRepository.save(image);

        return listImages(productId);
    }

    @Transactional
    public List<ProductImageResponse> deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository
                .findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + imageId));
        productImageRepository.delete(image);
        return listImages(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> listImages(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId).stream()
                .map(ProductImageResponse::fromEntity)
                .toList();
    }
}
