package com.ecommerce.gabrielportari.e_commerce_api.product.controller;

import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ImageUploadResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.service.FileStorageService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/upload-image")
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        return new ImageUploadResponse(fileStorageService.store(file));
    }
}
