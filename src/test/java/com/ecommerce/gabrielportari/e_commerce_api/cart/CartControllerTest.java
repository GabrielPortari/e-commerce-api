package com.ecommerce.gabrielportari.e_commerce_api.cart;

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
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CartControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product productWithStock(int stock) {
        Category category = categoryRepository.save(Category.builder().name("Eletrônicos").build());
        return productRepository.save(Product.builder()
                .name("Fone Bluetooth")
                .price(new BigDecimal("199.90"))
                .stock(stock)
                .category(category)
                .active(true)
                .build());
    }

    private String newSessionId() {
        return UUID.randomUUID().toString();
    }

    @Test
    void getCart_withoutSessionHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isBadRequest());
    }

    @Test
    void getCart_createsEmptyCartOnFirstAccess() throws Exception {
        mockMvc.perform(get("/api/cart").header("X-Session-Id", newSessionId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void addItem_withEnoughStock_addsToCart() throws Exception {
        Product product = productWithStock(10);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", newSessionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(399.80));
    }

    @Test
    void addItem_beyondAvailableStock_returns400() throws Exception {
        Product product = productWithStock(1);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", newSessionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto Fone Bluetooth"));
    }

    @Test
    void addItem_sameProductTwice_sumsQuantityInsteadOfDuplicating() throws Exception {
        Product product = productWithStock(10);
        String sessionId = newSessionId();

        mockMvc.perform(post("/api/cart/items")
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"));

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void updateItem_beyondStock_returns400() throws Exception {
        Product product = productWithStock(3);
        String sessionId = newSessionId();

        String response = mockMvc.perform(post("/api/cart/items")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":1}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long itemId = objectMapper.readTree(response).get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + itemId)
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clearCart_removesAllItems() throws Exception {
        Product product = productWithStock(5);
        String sessionId = newSessionId();

        mockMvc.perform(post("/api/cart/items")
                .header("X-Session-Id", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":" + product.getId() + ",\"quantity\":1}"));

        mockMvc.perform(delete("/api/cart").header("X-Session-Id", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
