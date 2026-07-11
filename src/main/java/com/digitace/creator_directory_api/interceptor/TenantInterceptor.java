package com.digitace.creator_directory_api.interceptor;

import com.digitace.creator_directory_api.context.TenantContext;
import com.digitace.creator_directory_api.domain.User;
import com.digitace.creator_directory_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdHeader = request.getHeader("X-User-Id");

        // 1. Block requests with no identification
        if (userIdHeader == null || userIdHeader.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-User-Id header");
            return false;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);

            // 2. Authenticate the User against the database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // 3. Lock the Agency ID and Role into the ThreadLocal Vault
            TenantContext.setAgencyId(user.getAgency().getId());
            TenantContext.setRole(user.getRole()); // <-- VULNERABILITY SEALED

            return true; // The request is cleared to hit the controllers

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid X-User-Id format or User does not exist");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // CRITICAL: Wipe the vault to prevent cross-contamination between threads
        TenantContext.clear();
    }
}