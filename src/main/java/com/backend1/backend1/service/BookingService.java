package com.backend1.backend1.service;

import com.backend1.backend1.dto.BookingDTO;
import com.backend1.backend1.exception.BookingConflictException;
import com.backend1.backend1.exception.BookingValidationException;
import com.backend1.backend1.model.Booking;
import com.backend1.backend1.model.Customer;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;

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
    public void save(Long bookingId, Long customerId, Long roomId,
                     LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingValidationException("Utcheckningsdatum måste vara efter incheckningsdatum");
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BookingValidationException("Kund hittades inte"));
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
        b.setCustomer(customer);
        b.setRoom(room);
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setNumberOfGuests(numberOfGuests);
        bookingRepository.save(b);
    }

    @Transactional
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return bookingRepository.count();
    }

    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        if (b.getCustomer() != null) {
            dto.setCustomerId(b.getCustomer().getId());
            dto.setCustomerFullName(b.getCustomer().getFullName());
        }
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
