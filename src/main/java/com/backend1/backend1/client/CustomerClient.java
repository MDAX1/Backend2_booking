package com.backend1.backend1.client;

import com.backend1.backend1.dto.CustomerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class CustomerClient {

    private final RestClient restClient;
    private final String baseUrl;

    public CustomerClient(RestClient restClient,
                          @Value("${customer.service.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    /**
     * Hämtar en kund från kundtjänsten via GET /api/customers/{id}.
     * Returnerar Optional.empty() om kunden inte finns (HTTP 404).
     */
    public Optional<CustomerResponse> getCustomer(Long customerId) {
        CustomerResponse customer = restClient.get()
                .uri(baseUrl + "/api/customers/{id}", customerId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    // Om 404 (kunden saknas), kasta inget fel här – vi returnerar Optional.empty() nedan
                })
                .body(CustomerResponse.class);

        return Optional.ofNullable(customer);
    }
}