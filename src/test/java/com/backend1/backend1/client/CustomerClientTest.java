package com.backend1.backend1.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CustomerClientTest {
    private MockRestServiceServer server;
    private CustomerClient client;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CustomerClient(builder.build(), "http://customer-service:8080");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void relaysTheAuthenticatedCallersTokenWithoutLeakingItToTheNextRequest() {
        authenticate("customer-issued-token", true);
        server.expect(requestTo("http://customer-service:8080/api/customers/42"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer customer-issued-token"))
                .andRespond(withSuccess("{\"id\":42,\"deleted\":false}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://customer-service:8080/api/customers/43"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("{\"id\":43,\"deleted\":false}", MediaType.APPLICATION_JSON));

        assertThat(client.getCustomer(42L)).hasValueSatisfying(customer -> assertThat(customer.id()).isEqualTo(42L));
        SecurityContextHolder.clearContext();
        assertThat(client.getCustomer(43L)).isPresent();
        server.verify();
    }

    @Test
    void doesNotRelayAnUnauthenticatedJwt() {
        authenticate("untrusted-token", false);
        server.expect(requestTo("http://customer-service:8080/api/customers/42"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("{\"id\":42}", MediaType.APPLICATION_JSON));
        client.getCustomer(42L);
        server.verify();
    }

    private void authenticate(String tokenValue, boolean authenticated) {
        Jwt jwt = Jwt.withTokenValue(tokenValue).header("alg", "HS256").subject("admin")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of());
        authentication.setAuthenticated(authenticated);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
