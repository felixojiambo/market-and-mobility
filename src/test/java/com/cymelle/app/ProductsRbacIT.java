package com.cymelle.app;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductsRbacIT extends IntegrationTestBase {

    @Test
    void customer_cannot_create_product_403() throws Exception {
        String customerToken = registerCustomerAndGetAccessToken("cust@cymelle.test", "Pass12345!");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Item A",
                                  "description":"Desc",
                                  "price": 10.00,
                                  "stockQuantity": 5,
                                  "category":"Test"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_create_product_201() throws Exception {
        String adminToken = adminAccessToken();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Item B",
                                  "description":"Desc",
                                  "price": 20.00,
                                  "stockQuantity": 10,
                                  "category":"Test"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
