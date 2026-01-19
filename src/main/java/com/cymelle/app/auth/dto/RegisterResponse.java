package com.cymelle.app.auth.dto;

import com.cymelle.app.users.Role;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
