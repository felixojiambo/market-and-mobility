//package com.cymelle.app;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class RidesIT extends IntegrationTestBase {
//
//    @Test
//    void customer_requests_ride_201() throws Exception {
//        String token = registerCustomerAndGetAccessToken("ride1@cymelle.test", "Pass12345!");
//
//        mockMvc.perform(post("/api/v1/rides")
//                        .header("Authorization", bearer(token))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "pickupLocation":"Westlands",
//                                  "dropoffLocation":"CBD",
//                                  "fare": 450.00
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("REQUESTED"));
//    }
//
//    @Test
//    void customer_views_own_ride_200() throws Exception {
//        String token = registerCustomerAndGetAccessToken("ride2@cymelle.test", "Pass12345!");
//
//        var res = mockMvc.perform(post("/api/v1/rides")
//                        .header("Authorization", bearer(token))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "pickupLocation":"A",
//                                  "dropoffLocation":"B",
//                                  "fare": 100.00
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long rideId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
//
//        mockMvc.perform(get("/api/v1/rides/" + rideId)
//                        .header("Authorization", bearer(token)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void customer_cannot_view_others_ride_403() throws Exception {
//        String userA = registerCustomerAndGetAccessToken("ride3a@cymelle.test", "Pass12345!");
//        String userB = registerCustomerAndGetAccessToken("ride3b@cymelle.test", "Pass12345!");
//
//        var res = mockMvc.perform(post("/api/v1/rides")
//                        .header("Authorization", bearer(userA))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "pickupLocation":"A",
//                                  "dropoffLocation":"B",
//                                  "fare": 100.00
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long rideId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
//
//        mockMvc.perform(get("/api/v1/rides/" + rideId)
//                        .header("Authorization", bearer(userB)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void admin_updates_ride_status_200_and_transitions_enforced() throws Exception {
//        String adminToken = adminAccessToken();
//        String userToken = registerCustomerAndGetAccessToken("ride4@cymelle.test", "Pass12345!");
//
//        var res = mockMvc.perform(post("/api/v1/rides")
//                        .header("Authorization", bearer(userToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "pickupLocation":"A",
//                                  "dropoffLocation":"B",
//                                  "fare": 100.00
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        long rideId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
//
//        // REQUESTED -> ACCEPTED ok
//        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""{"status":"ACCEPTED"}"""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("ACCEPTED"));
//
//        // ACCEPTED -> COMPLETED ok
//        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""{"status":"COMPLETED"}"""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("COMPLETED"));
//
//        // COMPLETED -> ACCEPTED invalid => 409
//        mockMvc.perform(patch("/api/v1/rides/" + rideId + "/status")
//                        .header("Authorization", bearer(adminToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""{"status":"ACCEPTED"}"""))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    void ride_search_paginated_works() throws Exception {
//        String token = registerCustomerAndGetAccessToken("ride5@cymelle.test", "Pass12345!");
//
//        for (int i = 0; i < 3; i++) {
//            mockMvc.perform(post("/api/v1/rides")
//                            .header("Authorization", bearer(token))
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                    {
//                                      "pickupLocation":"P%d",
//                                      "dropoffLocation":"D%d",
//                                      "fare": 50.00
//                                    }
//                                    """.formatted(i, i)))
//                    .andExpect(status().isCreated());
//        }
//
//        mockMvc.perform(get("/api/v1/rides?page=0&size=2")
//                        .header("Authorization", bearer(token)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(2));
//    }
//}
