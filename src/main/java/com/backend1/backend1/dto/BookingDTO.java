package com.backend1.backend1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
public class BookingDTO {
    private Long id;
    private Long customerId;
    private Long roomId;
    private String roomNumber;
    private String roomTypeDisplayName;
    private BigDecimal pricePerNight;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int numberOfGuests;

    public long getNights() {
        return checkIn != null && checkOut != null ? ChronoUnit.DAYS.between(checkIn, checkOut) : 0;
    }

    public BigDecimal getTotalPrice() {
        return pricePerNight != null ? pricePerNight.multiply(BigDecimal.valueOf(getNights())) : BigDecimal.ZERO;
    }
}
