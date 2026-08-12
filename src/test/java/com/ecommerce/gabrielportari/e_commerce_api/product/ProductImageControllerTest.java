package com.ecommerce.gabrielportari.e_commerce_api.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;

class ProductImageControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product product() {
        Category category = categoryRepository.save(Category.builder().name("Eletrônicos").build());
        return productRepository.save(Product.builder()
                .name("Fone Bluetooth")
                .price(new BigDecimal("199.90"))
                .stock(10)
                .category(category)
                .active(true)
                .build());
    }

    @Test
    void addImage_withoutToken_isForbidden() throws Exception {
        Product product = product();
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/products/" + product.getId() + "/images").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    void addImage_withAdminToken_appendsToGallery() throws Exception {
        Product product = product();
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/products/" + product.getId() + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].displayOrder").value(0))
                .andExpect(jsonPath("$[0].imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/")));

        flushAndClearPersistenceContext();

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(1));
    }

    @Test
    void deleteImage_removesFromGallery() throws Exception {
        Product product = product();
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        String token = adminToken();

        String response = mockMvc.perform(multipart("/api/products/" + product.getId() + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long imageId = objectMapper.readTree(response).get(0).get("id").asLong();

        mockMvc.perform(delete("/api/products/" + product.getId() + "/images/" + imageId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addImage_afterDeletingEarlierImage_doesNotReuseDisplayOrder() throws Exception {
        Product product = product();
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        String token = adminToken();

        String firstResponse = mockMvc.perform(multipart("/api/products/" + product.getId() + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long firstImageId = objectMapper.readTree(firstResponse).get(0).get("id").asLong();

        mockMvc.perform(multipart("/api/products/" + product.getId() + "/images")
                .file(file)
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(delete("/api/products/" + product.getId() + "/images/" + firstImageId)
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(multipart("/api/products/" + product.getId() + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[1].displayOrder").value(2));
    }
}
