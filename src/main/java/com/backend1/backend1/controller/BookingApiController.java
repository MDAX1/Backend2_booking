package com.backend1.backend1.controller;

import com.backend1.backend1.dto.BookingCountResponse;
import com.backend1.backend1.dto.BookingCreateRequest;
import com.backend1.backend1.dto.BookingDTO;
import com.backend1.backend1.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {

    private final BookingService bookingService;

    @GetMapping("/count")
    public BookingCountResponse count(
            @RequestParam Long customerId,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status) {
        long count = bookingService.countByCustomerIdAndStatus(customerId, status);
        return new BookingCountResponse(count);
    }

    @PostMapping
    public ResponseEntity<BookingDTO> create(@Valid @RequestBody BookingCreateRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        Long id = bookingService.save(null, request.customerId(), request.roomId(),
                request.checkIn(), request.checkOut(), request.numberOfGuests(), jwt.getTokenValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.findById(id));
    }
}