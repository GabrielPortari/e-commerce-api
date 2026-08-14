package com.ecommerce.gabrielportari.e_commerce_api.review;

import static org.hamcrest.Matchers.nullValue;
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
    void create_onInactiveProduct_returns404() throws Exception {
        Product product = product();
        product.setActive(false);
        productRepository.save(product);

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"authorName":"Ana","rating":5}
                                """))
                .andExpect(status().isNotFound());
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
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].authorName").value("Beto"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

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
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void findByProduct_withNoReviews_hasNullAverageRating() throws Exception {
        Product product = product();

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.reviewCount").value(0));
    }

    @Test
    void findByProduct_paginatesAndSortsByRating() throws Exception {
        Product product = product();
        reviewRepository.save(
                Review.builder().product(product).authorName("Ana").rating(3).build());
        reviewRepository.save(
                Review.builder().product(product).authorName("Beto").rating(5).build());
        reviewRepository.save(
                Review.builder().product(product).authorName("Caio").rating(1).build());

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "highest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].authorName").value("Beto"))
                .andExpect(jsonPath("$.content[1].authorName").value("Ana"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "lowest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].authorName").value("Caio"))
                .andExpect(jsonPath("$.content[1].authorName").value("Ana"));

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "highest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].authorName").value("Caio"));
    }

    @Test
    void findByProduct_withNonNumericPage_returns400() throws Exception {
        Product product = product();

        mockMvc.perform(get("/api/products/" + product.getId() + "/reviews").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_sameIpTwiceOnSameProduct_returns409ButDifferentIpIsAllowed() throws Exception {
        Product product = product();
        String body =
                """
                {"authorName":"Ana","rating":5,"comment":"Ótimo produto"}
                """;

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        }))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Você já avaliou este produto."));

        mockMvc.perform(post("/api/products/" + product.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.2");
                            return request;
                        }))
                .andExpect(status().isCreated());
    }
}
