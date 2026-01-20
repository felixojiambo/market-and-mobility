package com.cymelle.app.config;

import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;
import com.cymelle.app.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String email = "admin@cymelle.com";

        if (userRepository.existsByEmail(email)) return;

        AppUser admin = AppUser.create(
                email,
                passwordEncoder.encode("Admin@123"),
                "Admin",
                "User",
                Role.ADMIN
        );

        userRepository.save(admin);
    }
}
