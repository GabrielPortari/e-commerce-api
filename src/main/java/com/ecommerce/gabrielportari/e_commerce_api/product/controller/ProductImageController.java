package com.ecommerce.gabrielportari.e_commerce_api.product.controller;

import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ImageUploadResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ProductImageResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.service.FileStorageService;
import com.ecommerce.gabrielportari.e_commerce_api.product.service.ProductGalleryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final FileStorageService fileStorageService;
    private final ProductGalleryService productGalleryService;

    @PostMapping("/upload-image")
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        return new ImageUploadResponse(fileStorageService.store(file));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<List<ProductImageResponse>> addImage(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productGalleryService.addImage(id, file));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public List<ProductImageResponse> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        return productGalleryService.deleteImage(id, imageId);
    }
}
