package com.cymelle.app.auth;

import com.cymelle.app.auth.dto.AuthResponse;
import com.cymelle.app.auth.dto.LoginRequest;
import com.cymelle.app.auth.dto.RefreshRequest;
import com.cymelle.app.auth.dto.RegisterRequest;
import com.cymelle.app.common.exception.ConflictException;
import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.security.CustomUserDetails;
import com.cymelle.app.security.JwtService;
import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;
import com.cymelle.app.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisRefreshTokenStore refreshTokenStore;

    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.access-expiry-minutes}")
    private long accessExpiryMinutes;

    @Value("${app.jwt.refresh-expiry-days}")
    private long refreshExpiryDays;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        AppUser user = AppUser.create(request.getEmail(), hash, Role.CUSTOMER);
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            );

            authenticationManager.authenticate(authToken);

            AppUser user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NotFoundException("Invalid credentials"));

            return issueTokens(user);

        } catch (AuthenticationException ex) {
            throw new NotFoundException("Invalid credentials");
        }
    }

    public AuthResponse refresh(RefreshRequest request) {
        Long userId = refreshTokenStore.getUserIdIfValid(request.getRefreshToken())
                .orElseThrow(() -> new NotFoundException("Invalid refresh token"));

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Invalid refresh token"));

        // rotate: revoke old token, issue new token
        refreshTokenStore.revoke(request.getRefreshToken());

        return issueTokens(user);
    }

    private AuthResponse issueTokens(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = refreshTokenStore.issue(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .accessExpiresIn(accessExpiryMinutes * 60)
                .refreshExpiresIn(refreshExpiryDays * 24 * 60 * 60)
                .build();
    }
}
