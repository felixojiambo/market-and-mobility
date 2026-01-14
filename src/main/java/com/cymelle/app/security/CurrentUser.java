package com.cymelle.app.security;

import com.cymelle.app.common.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {}

    public static CustomUserDetails require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new NotFoundException("Unauthorized");
        }
        return user;
    }

    public static Long id() {
        return require().getId();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(require().getRole());
    }
}
