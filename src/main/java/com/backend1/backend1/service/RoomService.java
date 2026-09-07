package com.backend1.backend1.service;

import com.backend1.backend1.dto.RoomDTO;
import com.backend1.backend1.form.RoomForm;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<RoomDTO> findAll() {
        return roomRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RoomDTO findById(Long id) {
        return roomRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Rum med id " + id + " hittades inte"));
    }

    @Transactional
    public void save(RoomForm form) {
        roomRepository.save(toEntity(form));
    }

    @Transactional
    public void delete(Long id) {
        if (bookingRepository.existsByRoomId(id)) {
            throw new IllegalStateException("Kan inte ta bort rum som har aktiva bokningar");
        }
        roomRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return roomRepository.count();
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> findAvailableByDates(LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        List<Long> bookedIds = bookingRepository
                .findByCheckInBeforeAndCheckOutAfter(checkOut, checkIn)
                .stream().map(b -> b.getRoom().getId()).toList();
        List<Room> rooms = bookedIds.isEmpty()
                ? roomRepository.findAll()
                : roomRepository.findByIdNotIn(bookedIds);
        return rooms.stream()
                .filter(r -> r.getCapacity() >= numberOfGuests)
                .map(this::toDTO)
                .toList();
    }

    private RoomDTO toDTO(Room r) {
        RoomDTO dto = new RoomDTO();
        dto.setId(r.getId());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setType(r.getType());
        dto.setExtraBeds(r.getExtraBeds());
        dto.setPricePerNight(r.getPricePerNight());
        dto.setCapacity(r.getCapacity());
        dto.setTypeDescription(buildTypeDescription(r));
        return dto;
    }

    private static String buildTypeDescription(Room r) {
        if (r.getType() == RoomType.SINGLE) return "Enkelrum (1 person)";
        String extra = switch (r.getExtraBeds()) {
            case 1 -> "1 extrasäng";
            case 2 -> "2 extrasängar";
            default -> "inga extrasängar";
        };
        return "Dubbelrum, " + extra + " (max " + r.getCapacity() + " pers.)";
    }

    private Room toEntity(RoomForm form) {
        Room r = new Room();
        r.setId(form.getId());
        r.setRoomNumber(form.getRoomNumber());
        r.setType(form.getType());
        r.setExtraBeds(form.getType() == RoomType.SINGLE ? 0 : form.getExtraBeds());
        r.setPricePerNight(form.getPricePerNight());
        return r;
    }
}
