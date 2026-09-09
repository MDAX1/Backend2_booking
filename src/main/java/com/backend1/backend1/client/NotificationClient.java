package com.backend1.backend1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public NotificationClient(RestClient restClient,
                              @Value("${notification.service.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public record NotificationRequest(Long customerId, Long bookingId, LocalDate checkIn, LocalDate checkOut) {}

    /**
     * Skickar en bokningsbekräftelse till notifieringstjänsten.
     * Detta är "best effort": om notifieringstjänsten är nere eller svarar med fel
     * ska INTE hela bokningen misslyckas, vi loggar bara problemet och går vidare.
     */
    public void sendBookingConfirmation(Long customerId, Long bookingId,
                                        LocalDate checkIn, LocalDate checkOut, String token) {
        try {
            restClient.post()
                    .uri(baseUrl + "/api/notifications")
                    .headers(h -> h.setBearerAuth(token))
                    .body(new NotificationRequest(customerId, bookingId, checkIn, checkOut))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Kunde inte skicka bokningsbekräftelse för bokning {}: {}", bookingId, e.getMessage());
        }
    }
}