package com.backend1.backend1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new SessionBearerTokenResolver();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, BearerTokenResolver bearerTokenResolver) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Sök och lista är bara läsning - ingen token krävs
                        .requestMatchers(HttpMethod.POST, "/bookings/search").permitAll()
                        // Skapa, ändra och avboka ändrar data - kräver giltig token
                        .requestMatchers(HttpMethod.POST, "/bookings", "/bookings/*", "/bookings/*/delete", "/api/bookings")
                        .authenticated()
                        // Allt annat (visning, /api/bookings/count som kundtjänsten anropar, statiska filer)
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver)
                        .jwt(Customizer.withDefaults()))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login")))
                .build();
    }
}