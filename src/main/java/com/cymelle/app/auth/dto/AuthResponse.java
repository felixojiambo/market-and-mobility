package com.cymelle.app.auth.dto;

import com.cymelle.app.users.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Role role;
    private long accessExpiresIn;   // seconds
    private long refreshExpiresIn;  // seconds
}
