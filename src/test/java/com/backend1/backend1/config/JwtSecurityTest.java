package com.backend1.backend1.config;

import com.backend1.backend1.controller.BookingApiController;
import com.backend1.backend1.controller.BookingController;
import com.backend1.backend1.service.BookingService;
import com.backend1.backend1.service.RoomService;
import com.backend1.backend1.service.SearchService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BookingApiController.class, BookingController.class}, properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.issuer=pensionat-customer-service",
        "app.jwt.audience=pensionat"
})
@Import({JwtConfig.class, SecurityConfig.class})
class JwtSecurityTest {
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String PATH = "/api/bookings/count?customerId=42&status=ACTIVE";

    @Autowired MockMvc mvc;
    @MockitoBean BookingService bookingService;
    @MockitoBean RoomService roomService;
    @MockitoBean SearchService searchService;

    @ParameterizedTest
    @ValueSource(strings = {"/bookings", "/bookings/42", "/bookings/42/delete",
            "/rooms", "/rooms/42", "/rooms/42/delete"})
    void dataChangingEndpointsRejectMissingAndInvalidTokens(String path) throws Exception {
        mvc.perform(post(path)).andExpect(status().isUnauthorized());
        mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token("wrong-signature")))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(bookingService, roomService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/bookings", "/bookings/42"})
    void acceptsCustomerTokenForCreatingAndUpdatingBookings(String path) throws Exception {
        mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token("valid"))
                        .param("customerId", "7").param("roomId", "3")
                        .param("checkIn", "2026-10-01").param("checkOut", "2026-10-03")
                        .param("numberOfGuests", "1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/bookings"));
        verify(bookingService).save(path.equals("/bookings") ? null : 42L, 7L, 3L,
                java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 3), 1);
    }

    @Test
    void acceptsCustomerTokenForCancellingBookings() throws Exception {
        mvc.perform(post("/bookings/42/delete").header(HttpHeaders.AUTHORIZATION, "Bearer " + token("valid")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/bookings"));
        verify(bookingService).delete(42L);
    }

    @Test
    void acceptsTokenWithTheCustomerServicesSigningContract() throws Exception {
        when(bookingService.countByCustomerIdAndStatus(42L, "ACTIVE")).thenReturn(3L);
        mvc.perform(get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token("valid")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(cookie().doesNotExist("JSESSIONID"));
        // Authentication must not survive into the next request.
        mvc.perform(get(PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mvc.perform(get(PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
        verifyNoInteractions(bookingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "not-yet-valid", "wrong-issuer", "wrong-audience",
            "missing-audience", "missing-expiration", "wrong-signature", "wrong-algorithm", "malformed"})
    void rejectsInvalidTokensBeforeCallingTheService(String variant) throws Exception {
        mvc.perform(get(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token(variant)))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(bookingService);
    }

    @Test
    void doesNotAcceptTokensInTheQueryString() throws Exception {
        mvc.perform(get(PATH).param("access_token", token("valid")))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(bookingService);
    }

    @Test
    void everyApiPathRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/future-endpoint")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not base64", "c2hvcnQ="})
    void refusesMissingMalformedOrShortSecrets(String secret) {
        assertThatThrownBy(() -> new JwtConfig().jwtDecoder(secret, "issuer", "audience"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    private String token(String variant) throws JOSEException {
        if (variant.equals("malformed")) return "not-a-jwt";
        Instant now = Instant.now();
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(variant.equals("wrong-issuer") ? "other-service" : "pensionat-customer-service")
                .subject("admin")
                .issueTime(Date.from(now.minusSeconds(600)));
        if (!variant.equals("missing-audience")) {
            claims.audience(variant.equals("wrong-audience") ? "other-audience" : "pensionat");
        }
        if (!variant.equals("missing-expiration")) {
            claims.expirationTime(Date.from(now.plusSeconds(variant.equals("expired") ? -300 : 3600)));
        }
        if (variant.equals("not-yet-valid")) claims.notBeforeTime(Date.from(now.plusSeconds(300)));
        byte[] key = Base64.getDecoder().decode(SECRET);
        if (variant.equals("wrong-signature")) key[0] ^= 1;
        JWSAlgorithm algorithm = JWSAlgorithm.HS256;
        if (variant.equals("wrong-algorithm")) {
            algorithm = JWSAlgorithm.HS512;
            key = new byte[64];
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm), claims.build());
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }
}
