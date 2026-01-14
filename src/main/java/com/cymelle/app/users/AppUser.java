package com.cymelle.app.users;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "app_users")
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String passwordHash;
    private Role role;
    private Instant createdAt;

    protected AppUser() {}

    public static AppUser create(String email, String passwordHash, Role role) {
        AppUser user = new AppUser();
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

