package com.ecommerce.gabrielportari.e_commerce_api.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.gabrielportari.e_commerce_api.AbstractIntegrationTest;
import com.ecommerce.gabrielportari.e_commerce_api.category.entity.Category;
import com.ecommerce.gabrielportari.e_commerce_api.category.repository.CategoryRepository;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CategoryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findAll_returnsPublicly() throws Exception {
        categoryRepository.save(Category.builder().name("Eletrônicos").build());

        // Índice não é confiável: a categoria padrão "Geral" (seed da migration V9) também
        // aparece na lista, então buscamos pelo nome em vez de assumir a posição.
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Eletrônicos')]").exists());
    }

    @Test
    void create_withoutToken_isForbidden() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Roupas\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withAdminToken_createsCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Roupas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Roupas"));
    }

    @Test
    void create_withDuplicateName_returns400() throws Exception {
        categoryRepository.save(Category.builder().name("Roupas").build());

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"roupas\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma categoria com esse nome"));
    }

    @Test
    void update_renamesCategory() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Eletrônicos").build());

        mockMvc.perform(put("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Eletrônicos e Informática\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Eletrônicos e Informática"));
    }

    @Test
    void delete_withoutLinkedProduct_removesCategory() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Vazia").build());

        mockMvc.perform(delete("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withLinkedProduct_reassignsProductsToDefaultCategory() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Eletrônicos").build());
        Product product = productRepository.save(Product.builder()
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stock(5)
                .category(category)
                .active(true)
                .build());

        mockMvc.perform(delete("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        Category defaultCategory = categoryRepository.findByIsDefaultTrue().orElseThrow();
        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getCategory().getId()).isEqualTo(defaultCategory.getId());
    }

    @Test
    void delete_defaultCategory_returns400() throws Exception {
        Category defaultCategory = categoryRepository.findByIsDefaultTrue().orElseThrow();

        mockMvc.perform(delete("/api/categories/" + defaultCategory.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A categoria padrão não pode ser removida"));
    }
}
