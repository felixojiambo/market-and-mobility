package com.cymelle.app;

import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;
import com.cymelle.app.users.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    // Postgres
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("cymelle_test")
            .withUsername("postgres")
            .withPassword("postgres");

    // Redis (needed because you chose Redis-only refresh tokens)
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Match your application.yml keys
        registry.add("app.jwt.secret", () -> "test-secret-test-secret-test-secret-test-secret");
        registry.add("app.jwt.access-expiry-minutes", () -> "60");
        registry.add("app.jwt.refresh-expiry-days", () -> "14");

        registry.add("redis.host", redis::getHost);
        registry.add("redis.port", () -> redis.getMappedPort(6379).toString());

        // Ensure schema is created in tests
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected UserRepository userRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAdminExists() {
        // Create an admin if not present (stable credentials for tests)
        userRepository.findByEmail("admin@cymelle.test").orElseGet(() -> {
            AppUser admin = AppUser.create(
                    "admin@cymelle.test",
                    passwordEncoder.encode("AdminPass123!"),
                    Role.ADMIN
            );
            return userRepository.save(admin);
        });
    }

    protected String registerCustomerAndGetAccessToken(String email, String password) throws Exception {
        // Register
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk()); // you currently return tokens; if you return 201, change accordingly

        // Login to get fresh token
        return loginAndGetAccessToken(email, password);
    }

    protected String loginAndGetAccessToken(String email, String password) throws Exception {
        var res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());

        // Support either "accessToken" or "token" (in case you renamed DTO fields)
        if (json.hasNonNull("accessToken")) return json.get("accessToken").asText();
        if (json.hasNonNull("token")) return json.get("token").asText();

        throw new IllegalStateException("No access token found in login response");
    }

    protected String adminAccessToken() throws Exception {
        return loginAndGetAccessToken("admin@cymelle.test", "AdminPass123!");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
