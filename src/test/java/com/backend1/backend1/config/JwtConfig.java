package com.backend1.backend1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Bokningstjänsten UTFÄRDAR aldrig egna tokens - bara kundtjänsten gör det.
 * Vi behöver bara kunna VALIDERA tokens som kundtjänsten redan signerat,
 * med samma delade hemlighet (JWT_SECRET) som alla tre tjänster känner till.
 */
@Configuration
public class JwtConfig {

    private final String secret;
    private final String issuer;
    private final String audience;

    public JwtConfig(@Value("${app.jwt.secret}") String secret,
                     @Value("${app.jwt.issuer}") String issuer,
                     @Value("${app.jwt.audience}") String audience) {
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience() != null && jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_audience", "Wrong audience in token", null));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                new JwtIssuerValidator(issuer),
                audienceValidator));

        return decoder;
    }
}