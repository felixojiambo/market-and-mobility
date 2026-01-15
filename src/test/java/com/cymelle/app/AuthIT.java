package com.cymelle.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthIT extends IntegrationTestBase {

    @Test
    void register_and_login_returns_jwt() throws Exception {
        String token = registerCustomerAndGetAccessToken("user1@cymelle.test", "Pass12345!");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // basic JWT shape check
    }
}
