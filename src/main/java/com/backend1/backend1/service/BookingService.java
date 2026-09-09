package com.backend1.backend1.service;

import com.backend1.backend1.client.CustomerClient;
import com.backend1.backend1.client.NotificationClient;
import com.backend1.backend1.dto.BookingDTO;
import com.backend1.backend1.dto.CustomerResponse;
import com.backend1.backend1.exception.BookingConflictException;
import com.backend1.backend1.exception.BookingValidationException;
import com.backend1.backend1.model.Booking;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    @Transactional(readOnly = true)
    public List<BookingDTO> findAll() {
        return bookingRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BookingDTO findById(Long id) {
        return bookingRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Bokning med id " + id + " hittades inte"));
    }

    @Transactional
    public Long save(Long bookingId, Long customerId, Long roomId,
                     LocalDate checkIn, LocalDate checkOut, int numberOfGuests, String token) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingValidationException("Utcheckningsdatum måste vara efter incheckningsdatum");
        }
        if (customerId == null) {
            throw new BookingValidationException("Kund-ID måste anges");
        }
        // Verifiera att kunden finns i kundtjänsten
        CustomerResponse customer = customerClient.getCustomer(customerId, token)
                .orElseThrow(() -> new BookingValidationException("Kunden med ID " + customerId + " hittades inte i kundtjänsten"));
        if (customer.deleted()) {
            throw new BookingValidationException("Kunden är inaktiv/borttagen och kan inte göra bokningar");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BookingValidationException("Rum hittades inte"));
        if (room.getCapacity() < numberOfGuests) {
            int capacity = room.getCapacity();
            throw new BookingValidationException("Rummet har plats för " + capacity
                    + (capacity == 1 ? " person" : " personer") + ", inte " + numberOfGuests);
        }
        long overlaps = bookingId == null
                ? bookingRepository.countByRoomIdAndCheckInBeforeAndCheckOutAfter(roomId, checkOut, checkIn)
                : bookingRepository.countByRoomIdAndCheckInBeforeAndCheckOutAfterAndIdNot(roomId, checkOut, checkIn, bookingId);
        if (overlaps > 0) {
            throw new BookingConflictException("Rummet är redan bokat för de valda datumen");
        }
        Booking b = new Booking();
        b.setId(bookingId);
        b.setCustomerId(customerId);
        b.setRoom(room);
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setNumberOfGuests(numberOfGuests);
        Booking saved = bookingRepository.save(b);

        notificationClient.sendBookingConfirmation(customerId, saved.getId(), checkIn, checkOut, token);

        return saved.getId();
    }

    @Transactional
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return bookingRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByCustomerIdAndStatus(Long customerId, String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return bookingRepository.countByCustomerIdAndCheckOutGreaterThanEqual(customerId, LocalDate.now());
        }
        return bookingRepository.countByCustomerId(customerId);
    }

    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setCustomerId(b.getCustomerId());
        if (b.getRoom() != null) {
            dto.setRoomId(b.getRoom().getId());
            dto.setRoomNumber(b.getRoom().getRoomNumber());
            dto.setRoomTypeDisplayName(b.getRoom().getType() != null
                    ? b.getRoom().getType().getDisplayName() : "");
            dto.setPricePerNight(b.getRoom().getPricePerNight());
        }
        dto.setCheckIn(b.getCheckIn());
        dto.setCheckOut(b.getCheckOut());
        dto.setNumberOfGuests(b.getNumberOfGuests());
        return dto;
    }
}