package com.backend1.backend1.service;

import com.backend1.backend1.dto.RoomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchService {

    private final RoomService roomService;

    public List<RoomDTO> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        return roomService.findAvailableByDates(checkIn, checkOut, numberOfGuests);
    }
}
