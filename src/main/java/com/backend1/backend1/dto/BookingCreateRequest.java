package com.backend1.backend1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingCreateRequest(
        @NotNull Long customerId,
        @NotNull Long roomId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Min(1) int numberOfGuests
) {}
