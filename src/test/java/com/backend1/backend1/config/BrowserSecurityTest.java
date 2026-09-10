package com.backend1.backend1.config;

import com.backend1.backend1.client.AuthClient;
import com.backend1.backend1.controller.BookingApiController;
import com.backend1.backend1.controller.BookingController;
import com.backend1.backend1.controller.LoginController;
import com.backend1.backend1.controller.RoomController;
import com.backend1.backend1.service.BookingService;
import com.backend1.backend1.service.RoomService;
import com.backend1.backend1.service.SearchService;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BookingApiController.class, BookingController.class, RoomController.class, LoginController.class})
@Import({SecurityConfig.class, JwtConfig.class})
class BrowserSecurityTest {
    private static final String SECRET = "dGVzdGhlbWxpZ2hldC1lbmJhcnQtZm9yLXRlc3Rlci0zMmI=";
    private static final String COUNT = "/api/bookings/count?customerId=1";
    @Autowired MockMvc mvc;
    @MockitoBean BookingService bookings;
    @MockitoBean RoomService rooms;
    @MockitoBean SearchService search;
    @MockitoBean AuthClient auth;

    @ParameterizedTest
    @ValueSource(strings = {"/rooms", "/rooms/1", "/rooms/1/delete", "/bookings", "/bookings/1", "/bookings/1/delete"})
    void mutationsRequireLoginEvenWithValidCsrf(String path) throws Exception {
        mvc.perform(mutation(path).with(csrf()))
                .andExpect(status().isFound()).andExpect(redirectedUrl("http://localhost/login"));
        verifyNoInteractions(bookings, rooms);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/rooms", "/rooms/1", "/rooms/1/delete", "/bookings", "/bookings/1", "/bookings/1/delete"})
    void sessionTokenDoesNotBypassCsrf(String path) throws Exception {
        var session = session(token("valid"));
        mvc.perform(mutation(path).session(session)).andExpect(status().isForbidden());
        mvc.perform(mutation(path).session(session).with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());
        // An unrelated Authorization scheme must not exempt session authentication either.
        mvc.perform(mutation(path).session(session).header(HttpHeaders.AUTHORIZATION, "Basic ignored"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(bookings, rooms);
    }

    @Test
    void sessionAndCsrfAllowRoomAndBookingMutations() throws Exception {
        String jwt = token("valid");
        var session = session(jwt);
        mvc.perform(mutation("/rooms").session(session).with(csrf()))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/rooms"));
        mvc.perform(mutation("/bookings").session(session).with(csrf()))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/bookings"));
        mvc.perform(mutation("/rooms/1/delete").session(session).with(csrf())).andExpect(status().isFound());
        mvc.perform(mutation("/bookings/1/delete").session(session).with(csrf())).andExpect(status().isFound());
        verify(rooms).save(any());
        verify(rooms).delete(1L);
        verify(bookings).save(isNull(), eq(1L), eq(1L), any(), any(), eq(1), eq(jwt));
        verify(bookings).delete(1L);
    }

    @Test
    void apiRequiresAnExplicitHeaderAndDoesNotReuseSessions() throws Exception {
        var session = session(token("valid"));
        mvc.perform(get(COUNT)).andExpect(status().isUnauthorized());
        mvc.perform(get(COUNT).session(session)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/bookings").session(session).with(csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(COUNT).param("access_token", token("valid")))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(bookings);
    }

    @Test
    void explicitBearerHeadersWorkWithoutCsrfOrSession() throws Exception {
        String jwt = token("valid");
        mvc.perform(get(COUNT).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0))
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull());
        when(bookings.save(any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(1L);
        mvc.perform(post("/api/bookings").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType("application/json").content("""
                                {"customerId":1,"roomId":1,"checkIn":"2026-10-01","checkOut":"2026-10-03","numberOfGuests":1}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(mutation("/bookings").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isFound());
        mvc.perform(get(COUNT)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "wrong-signature", "wrong-issuer", "wrong-audience"})
    void invalidTokensAreRejectedForHeadersAndSessions(String variant) throws Exception {
        String jwt = token(variant);
        mvc.perform(get(COUNT).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
        mvc.perform(mutation("/bookings").session(session(jwt)).with(csrf()))
                .andExpect(status().isFound()).andExpect(redirectedUrl("http://localhost/login"));
        mvc.perform(get("/login").session(session(jwt))).andExpect(status().isOk());
        verifyNoInteractions(bookings);
    }

    @Test
    void renderedLoginFormSuppliesCsrfAndLoginRotatesSessionId() throws Exception {
        var result = mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        String previousId = session.getId();
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(result.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        String jwt = token("valid");
        when(auth.login("admin", "password")).thenReturn(jwt);
        mvc.perform(post("/login").session(session).param("_csrf", matcher.group(1))
                        .param("username", "admin").param("password", "password"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/bookings"));
        assertThat(session.getId()).isNotEqualTo(previousId);
        assertThat(session.getAttribute(SessionBearerTokenResolver.SESSION_TOKEN_KEY)).isEqualTo(jwt);
        var form = mvc.perform(get("/bookings/new").session(session)).andExpect(status().isOk()).andReturn();
        matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(form.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        mvc.perform(mutation("/bookings").session(session).param("_csrf", matcher.group(1)))
                .andExpect(status().isFound());
        verify(bookings).save(isNull(), eq(1L), eq(1L), any(), any(), eq(1), eq(jwt));
    }

    @Test
    void loginAndLogoutRequireCsrf() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "password"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(auth);
        var session = session(token("valid"));
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        assertThat(session.isInvalid()).isFalse();
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/login"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void searchRemainsPublicWithoutCsrf() throws Exception {
        mvc.perform(post("/bookings/search").param("checkIn", "2026-10-01")
                        .param("checkOut", "2026-10-03").param("numberOfGuests", "1"))
                .andExpect(status().isOk());
        verify(search).findAvailableRooms(any(), any(), eq(1));
    }

    private MockHttpServletRequestBuilder mutation(String path) {
        return post(path).param("customerId", "1").param("roomId", "1")
                .param("checkIn", "2026-10-01").param("checkOut", "2026-10-03")
                .param("numberOfGuests", "1").param("roomNumber", "101")
                .param("type", "SINGLE").param("extraBeds", "0").param("pricePerNight", "800");
    }

    private MockHttpSession session(String jwt) {
        var session = new MockHttpSession();
        session.setAttribute(SessionBearerTokenResolver.SESSION_TOKEN_KEY, jwt);
        return session;
    }

    private String token(String variant) throws Exception {
        var claims = new JWTClaimsSet.Builder().subject("admin")
                .issuer(variant.equals("wrong-issuer") ? "other" : "pensionat-customer-service")
                .audience(variant.equals("wrong-audience") ? "other" : "pensionat")
                .expirationTime(Date.from(Instant.now().plusSeconds(variant.equals("expired") ? -300 : 300)))
                .build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        byte[] key = Base64.getDecoder().decode(SECRET);
        if (variant.equals("wrong-signature")) key[0] ^= 1;
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }
}
