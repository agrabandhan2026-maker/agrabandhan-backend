package com.agrabandhan.common.config;

import com.agrabandhan.auth.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- Public endpoints (specific before broad) ---
                .requestMatchers("/auth/send-otp", "/auth/verify-otp", "/auth/refresh-token").permitAll()
                .requestMatchers("/health", "/health/**").permitAll()
                .requestMatchers("/error").permitAll()

                // Swagger / OpenAPI
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Actuator
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // --- Admin endpoints ---
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // --- Moderator endpoints ---
                .requestMatchers("/moderation/**").hasAnyRole("ADMIN", "MODERATOR")

                // --- All other endpoints require authentication ---
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(401);
                    objectMapper.writeValue(response.getOutputStream(),
                            Map.of("success", false, "message", "Authentication required"));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(403);
                    objectMapper.writeValue(response.getOutputStream(),
                            Map.of("success", false, "message", "Access denied"));
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
