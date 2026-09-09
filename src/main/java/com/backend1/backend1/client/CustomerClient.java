package com.backend1.backend1.client;

import com.backend1.backend1.dto.CustomerResponse;
import com.backend1.backend1.exception.CustomerServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

    public Optional<CustomerResponse> getCustomer(Long customerId, String token) {
        try {
            CustomerResponse customer = restClient.get()
                    .uri(baseUrl + "/api/customers/{id}", customerId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        // 404 fångas tyst, returnerar null som blir Optional.empty()
                    })
                    .body(CustomerResponse.class);

            return Optional.ofNullable(customer);
        } catch (RestClientException e) {
            // Om kundtjänsten är nere (connection refused, timeout osv) kastar vi 503-felet
            throw new CustomerServiceUnavailableException(
                    "Kundtjänsten är inte tillgänglig just nu. Försök igen senare.", e);
        }
    }
}