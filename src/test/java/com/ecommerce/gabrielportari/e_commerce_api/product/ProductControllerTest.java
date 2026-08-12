package com.ecommerce.gabrielportari.e_commerce_api.product;

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

class ProductControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category category() {
        return categoryRepository.save(Category.builder().name("Eletrônicos").build());
    }

    @Test
    void findAll_returnsOnlyActiveProducts() throws Exception {
        Category category = category();
        productRepository.save(Product.builder()
                .name("Ativo")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category)
                .active(true)
                .build());
        productRepository.save(Product.builder()
                .name("Inativo")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category)
                .active(false)
                .build());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ativo"));
    }

    @Test
    void findById_whenInactive_returns404() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Inativo")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category())
                .active(false)
                .build());

        mockMvc.perform(get("/api/products/" + product.getId())).andExpect(status().isNotFound());
    }

    @Test
    void findBySlug_returnsMatchingProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Produto Buscável")
                .slug("produto-buscavel")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category())
                .active(true)
                .build());

        mockMvc.perform(get("/api/products/slug/produto-buscavel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Produto Buscável"));
    }

    @Test
    void findBySlug_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/slug/inexistente")).andExpect(status().isNotFound());
    }

    @Test
    void create_generatesUniqueSlugFromName() throws Exception {
        Long categoryId = category().getId();
        String payload =
                """
                {"name":"Camiseta Polo","price":99.90,"stock":10,"categoryId":%d,"onSale":false,"featured":false}
                """
                        .formatted(categoryId);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("camiseta-polo"));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("camiseta-polo-2"));
    }

    @Test
    void create_withoutToken_isForbidden() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_onSaleWithoutDiscountPrice_returns400() throws Exception {
        Long categoryId = category().getId();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"name":"Camiseta","price":99.90,"stock":10,"categoryId":%d,"onSale":true,"featured":false}
                                """
                                        .formatted(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Preço promocional é obrigatório para produtos em promoção"));
    }

    @Test
    void create_withDiscountPriceNotLowerThanPrice_returns400() throws Exception {
        Long categoryId = category().getId();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"name":"Camiseta","price":99.90,"discountPrice":99.90,"stock":10,"categoryId":%d,"onSale":true,"featured":false}
                                """
                                        .formatted(categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Preço promocional deve ser menor que o preço original"));
    }

    @Test
    void create_withValidPromotion_persistsDiscountPrice() throws Exception {
        Long categoryId = category().getId();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"name":"Camiseta","price":99.90,"discountPrice":69.90,"stock":10,"categoryId":%d,"onSale":true,"featured":true}
                                """
                                        .formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.onSale").value(true))
                .andExpect(jsonPath("$.discountPrice").value(69.90))
                .andExpect(jsonPath("$.featured").value(true));
    }

    @Test
    void create_withoutCategory_returns400Validation() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"name":"Camiseta","price":99.90,"stock":10,"onSale":false,"featured":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_softDeletes_hiddenFromPublicButVisibleForAdmin() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Produto")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category())
                .active(true)
                .build());

        mockMvc.perform(delete("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + product.getId())).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/admin/products").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void reactivate_bringsBackDeactivatedProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Produto")
                .price(new BigDecimal("10.00"))
                .stock(1)
                .category(category())
                .active(false)
                .build());

        mockMvc.perform(put("/api/products/" + product.getId() + "/reactivate")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/products/" + product.getId())).andExpect(status().isOk());
    }
}
