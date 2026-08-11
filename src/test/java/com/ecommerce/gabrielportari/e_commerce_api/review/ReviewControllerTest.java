package com.ecommerce.gabrielportari.e_commerce_api.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.gabrielportari.e_commerce_api.AbstractIntegrationTest;
import com.ecommerce.gabrielportari.e_commerce_api.category.entity.Category;
import com.ecommerce.gabrielportari.e_commerce_api.category.repository.CategoryRepository;
import com.ecommerce.gabrielportari.e_commerce_api.product.entity.Product;
import com.ecommerce.gabrielportari.e_commerce_api.product.repository.ProductRepository;
import com.ecommerce.gabrielportari.e_commerce_api.review.entity.Review;
import com.ecommerce.gabrielportari.e_commerce_api.review.repository.ReviewRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ReviewControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

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
    void create_withoutToken_isAllowed() throws Exception {
        Product product = product();

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"authorName":"Ana","rating":5,"comment":"Ótimo produto"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Ana"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void create_withRatingOutOfRange_returns400() throws Exception {
        Product product = product();

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"authorName":"Ana","rating":9}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByProduct_returnsNewestFirst() throws Exception {
        Product product = product();
        reviewRepository.save(
                Review.builder().product(product).authorName("Ana").rating(4).build());
        reviewRepository.save(
                Review.builder().product(product).authorName("Beto").rating(2).build());

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        flushAndClearPersistenceContext();

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.averageRating").value(3.0));
    }

    @Test
    void delete_withoutToken_isForbidden() throws Exception {
        Product product = product();
        Review review =
                reviewRepository.save(Review.builder().product(product).authorName("Ana").rating(5).build());

        mockMvc.perform(delete("/api/reviews/" + review.getId())).andExpect(status().isForbidden());
    }

    @Test
    void delete_withAdminToken_removesReview() throws Exception {
        Product product = product();
        Review review =
                reviewRepository.save(Review.builder().product(product).authorName("Ana").rating(5).build());

        mockMvc.perform(delete("/api/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
