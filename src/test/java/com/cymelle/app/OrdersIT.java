//package com.cymelle.app;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class OrdersIT extends IntegrationTestBase {
//
//    private long createProductAsAdmin(String adminToken, String name, double price, int stock) throws Exception {
//        var res = mockMvc.perform(post("/api/v1/products")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name":"%s",
//                                  "description":"Desc",
//                                  "price": %.2f,
//                                  "stockQuantity": %d,
//                                  "category":"Test"
//                                }
//                                """.formatted(name, price, stock)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
//        return json.get("id").asLong();
//    }
//
//    @Test
//    void customer_places_order_201_and_stock_reduced() throws Exception {
//        String adminToken = adminAccessToken();
//        long productId = createProductAsAdmin(adminToken, "OrderProduct1", 30.00, 10);
//
//        String custToken = registerCustomerAndGetAccessToken("buyer@cymelle.test", "Pass12345!");
//
//        // place order qty=3
//        var orderRes = mockMvc.perform(post("/api/v1/orders")
//                        .header("Authorization", bearer(custToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"items":[{"productId":%d,"quantity":3}]}
//                                """.formatted(productId)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("PENDING"))
//                .andReturn();
//
//        JsonNode orderJson = objectMapper.readTree(orderRes.getResponse().getContentAsString());
//        long orderId = orderJson.get("id").asLong();
//
//        // verify product stock is now 7 (GET product)
//        var productRes = mockMvc.perform(get("/api/v1/products/" + productId))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        JsonNode productJson = objectMapper.readTree(productRes.getResponse().getContentAsString());
//        assertThat(productJson.get("stockQuantity").asInt()).isEqualTo(7);
//
//        // verify total cost = 30 * 3
//        assertThat(orderJson.get("totalCost").asDouble()).isEqualTo(90.00);
//
//        // sanity: customer can view own order
//        mockMvc.perform(get("/api/v1/orders/" + orderId)
//                        .header("Authorization", bearer(custToken)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void insufficient_stock_returns_409() throws Exception {
//        String adminToken = adminAccessToken();
//        long productId = createProductAsAdmin(adminToken, "OrderProduct2", 50.00, 2);
//
//        String custToken = registerCustomerAndGetAccessToken("buyer2@cymelle.test", "Pass12345!");
//
//        mockMvc.perform(post("/api/v1/orders")
//                        .header("Authorization", bearer(custToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"items":[{"productId":%d,"quantity":3}]}
//                                """.formatted(productId)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    void customer_cannot_view_others_order_403() throws Exception {
//        String adminToken = adminAccessToken();
//        long productId = createProductAsAdmin(adminToken, "OrderProduct3", 10.00, 10);
//
//        String userA = registerCustomerAndGetAccessToken("a@cymelle.test", "Pass12345!");
//        String userB = registerCustomerAndGetAccessToken("b@cymelle.test", "Pass12345!");
//
//        var orderRes = mockMvc.perform(post("/api/v1/orders")
//                        .header("Authorization", bearer(userA))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"items":[{"productId":%d,"quantity":1}]}
//                                """.formatted(productId)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long orderId = objectMapper.readTree(orderRes.getResponse().getContentAsString()).get("id").asLong();
//
//        mockMvc.perform(get("/api/v1/orders/" + orderId)
//                        .header("Authorization", bearer(userB)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void admin_updates_order_status_200_and_transitions_enforced() throws Exception {
//        String adminToken = adminAccessToken();
//        long productId = createProductAsAdmin(adminToken, "OrderProduct4", 15.00, 10);
//
//        String custToken = registerCustomerAndGetAccessToken("buyer3@cymelle.test", "Pass12345!");
//
//        var orderRes = mockMvc.perform(post("/api/v1/orders")
//                        .header("Authorization", bearer(custToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"items":[{"productId":%d,"quantity":2}]}
//                                """.formatted(productId)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long orderId = objectMapper.readTree(orderRes.getResponse().getContentAsString()).get("id").asLong();
//
//        // PENDING -> SHIPPED ok
//        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//        {"status":"SHIPPED"}
//        """)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("SHIPPED"));
//
//        // SHIPPED -> DELIVERED ok
//        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//        {"status":"DELIVERED"}
//        """)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("DELIVERED"));
//
//        // DELIVERED -> SHIPPED invalid => 409
//        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""{"status":"SHIPPED"}"""))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    void order_search_paginated_works() throws Exception {
//        String adminToken = adminAccessToken();
//        long p1 = createProductAsAdmin(adminToken, "OrderProduct5", 5.00, 100);
//
//        String custToken = registerCustomerAndGetAccessToken("buyer4@cymelle.test", "Pass12345!");
//
//        // create 3 orders
//        for (int i = 0; i < 3; i++) {
//            mockMvc.perform(post("/api/v1/orders")
//                            .header("Authorization", bearer(custToken))
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                    {"items":[{"productId":%d,"quantity":1}]}
//                                    """.formatted(p1)))
//                    .andExpect(status().isCreated());
//        }
//
//        // page size 2
//        mockMvc.perform(get("/api/v1/orders?page=0&size=2")
//                        .header("Authorization", bearer(custToken)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(2));
//    }
//
//    @Test
//    void pay_endpoint_updates_payment_status() throws Exception {
//        String adminToken = adminAccessToken();
//        long productId = createProductAsAdmin(adminToken, "OrderProduct6", 12.00, 10);
//
//        String custToken = registerCustomerAndGetAccessToken("buyer5@cymelle.test", "Pass12345!");
//
//        var orderRes = mockMvc.perform(post("/api/v1/orders")
//                        .header("Authorization", bearer(custToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"items":[{"productId":%d,"quantity":1}]}
//                                """.formatted(productId)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long orderId = objectMapper.readTree(orderRes.getResponse().getContentAsString()).get("id").asLong();
//
//        // explicit success=true
//        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
//                        .header("Authorization", bearer(custToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//        {"success":true}
//        """)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.paymentStatus").value("PAID"));
//    }
//}
