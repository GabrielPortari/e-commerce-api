package com.ecommerce.gabrielportari.e_commerce_api.product.controller;

import com.ecommerce.gabrielportari.e_commerce_api.product.dto.ProductResponse;
import com.ecommerce.gabrielportari.e_commerce_api.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAllForAdmin();
    }
}
