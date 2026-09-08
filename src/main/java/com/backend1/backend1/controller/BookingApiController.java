package com.backend1.backend1.controller;

import com.backend1.backend1.dto.BookingCountResponse;
import com.backend1.backend1.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}