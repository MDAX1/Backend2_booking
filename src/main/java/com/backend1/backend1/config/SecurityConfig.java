package com.backend1.backend1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                // API authentication is header-only, so a browser session cannot bypass CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new DefaultBearerTokenResolver())
                        .jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain browserFilterChain(HttpSecurity http) throws Exception {
        var loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/bookings/search", "POST")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/bookings/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/bookings", "/bookings/**", "/rooms", "/rooms/**")
                        .authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Spring exempts requests resolved here from CSRF. Only explicit
                        // Authorization headers may qualify, never a session's AUTH_TOKEN.
                        .bearerTokenResolver(new DefaultBearerTokenResolver())
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(loginEntryPoint)
                        .withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                            @Override
                            public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                                // Resolve session tokens only in the authentication filter;
                                // CSRF continues to use the header-only resolver above.
                                filter.setBearerTokenResolver(new SessionBearerTokenResolver());
                                return filter;
                            }
                        }))
                .exceptionHandling(e -> e.authenticationEntryPoint(loginEntryPoint))
                .logout(logout -> logout.logoutSuccessUrl("/login"))
                .build();
    }
}
