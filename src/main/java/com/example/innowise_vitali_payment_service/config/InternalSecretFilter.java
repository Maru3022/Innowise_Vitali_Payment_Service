package com.example.innowise_vitali_payment_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Internal-Secret";

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String headerValue = request.getHeader(HEADER_NAME);
        if (!internalSecret.equals(headerValue)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid " + HEADER_NAME + " header");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
