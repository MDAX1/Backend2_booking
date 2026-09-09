package com.backend1.backend1.client;

import com.backend1.backend1.dto.LoginRequest;
import com.backend1.backend1.dto.LoginResponse;
import com.backend1.backend1.exception.CustomerServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AuthClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AuthClient(RestClient restClient, @Value("${customer.service.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    /**
     * Loggar in mot kundtjänstens /api/auth/login åt användaren.
     * Kastar HttpClientErrorException.Unauthorized vidare orört (fel användarnamn/lösenord)
     * så att LoginController kan visa ett tydligt meddelande om just det.
     */
    public String login(String username, String password) {
        try {
            LoginResponse response = restClient.post()
                    .uri(baseUrl + "/api/auth/login")
                    .body(new LoginRequest(username, password))
                    .retrieve()
                    .body(LoginResponse.class);

            if (response == null || response.token() == null) {
                throw new CustomerServiceUnavailableException(
                        "Kundtjänsten svarade oväntat vid inloggning. Försök igen senare.");
            }
            return response.token();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw e;
        } catch (RestClientException e) {
            throw new CustomerServiceUnavailableException(
                    "Kundtjänsten är inte tillgänglig just nu. Försök igen senare.", e);
        }
    }
}
