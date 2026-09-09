package com.backend1.backend1.controller;

import com.backend1.backend1.client.CustomerClient;
import com.backend1.backend1.client.NotificationClient;
import com.backend1.backend1.dto.CustomerResponse;
import com.backend1.backend1.exception.CustomerServiceUnavailableException;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // CustomerClient och NotificationClient pratar med riktiga externa tjänster.
    // I ett integrationstest av bokningstjänsten ska vi INTE bero på att de
    // andra tjänsterna faktiskt kör – vi mockar dem och styr svaret själva.
    @MockitoBean
    private CustomerClient customerClient;

    @MockitoBean
    private NotificationClient notificationClient;

    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();

        room = new Room();
        room.setRoomNumber("101");
        room.setType(RoomType.SINGLE);
        room.setExtraBeds(0);
        room.setPricePerNight(new BigDecimal("800"));
        room = roomRepository.save(room);
    }

    private CustomerResponse validCustomer(Long id) {
        return new CustomerResponse(id, "Test", "Testsson", "test@test.se", "0700000000", "Gata 1", false);
    }

    private Map<String, Object> bookingRequest(long customerId) {
        return Map.of(
                "customerId", customerId,
                "roomId", room.getId(),
                "checkIn", LocalDate.now().plusDays(1).toString(),
                "checkOut", LocalDate.now().plusDays(3).toString(),
                "numberOfGuests", 1
        );
    }

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        when(customerClient.getCustomer(eq(1L), any())).thenReturn(Optional.of(validCustomer(1L)));

        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.roomId").value(room.getId()));
    }

    @Test
    void createBooking_overlappingDates_returns409() throws Exception {
        when(customerClient.getCustomer(eq(1L), any())).thenReturn(Optional.of(validCustomer(1L)));
        var request = bookingRequest(1L);

        // Första bokningen går igenom
        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Samma rum, samma datum igen -> konflikt
        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBooking_unknownCustomer_returns400() throws Exception {
        when(customerClient.getCustomer(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest(999L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_customerServiceDown_returns503() throws Exception {
        when(customerClient.getCustomer(eq(1L), any()))
                .thenThrow(new CustomerServiceUnavailableException("Kundtjänsten är inte tillgänglig just nu."));

        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest(1L))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void createBooking_noToken_returns401() throws Exception {
        // Utan giltig JWT ska anropet blockeras innan det ens når vår kod.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest(1L))))
                .andExpect(status().isUnauthorized());
    }
}