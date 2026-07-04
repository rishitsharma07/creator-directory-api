package com.digitace.creator_directory_api.config;

import com.digitace.creator_directory_api.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Enforce the Gatekeeper on all core API endpoints
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/creators/**", "/users/**");
    }
}