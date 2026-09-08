package com.backend1.backend1.repository;

import com.backend1.backend1.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCustomerId(Long customerId);

    boolean existsByRoomId(Long roomId);

    List<Booking> findByCheckInBeforeAndCheckOutAfter(LocalDate checkOut, LocalDate checkIn);

    long countByCustomerId(Long customerId);

    // Räkna aktiva bokningar (där checkOut är idag eller i framtiden)
    long countByCustomerIdAndCheckOutGreaterThanEqual(Long customerId, LocalDate checkOut);

    long countByRoomIdAndCheckInBeforeAndCheckOutAfter(
            Long roomId, LocalDate checkOut, LocalDate checkIn);

    long countByRoomIdAndCheckInBeforeAndCheckOutAfterAndIdNot(
            Long roomId, LocalDate checkOut, LocalDate checkIn, Long excludeId);
}
