package com.cymelle.app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "firstName is required")
    @Size(max = 80, message = "firstName must be <= 80 chars")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 80, message = "lastName must be <= 80 chars")
    private String lastName;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
    private String password;
}
