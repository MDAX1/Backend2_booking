package com.backend1.backend1.service;

import com.backend1.backend1.dto.RoomDTO;
import com.backend1.backend1.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private SearchService searchService;

    private final LocalDate IN  = LocalDate.of(2025, 8, 1);
    private final LocalDate OUT = LocalDate.of(2025, 8, 5);

    private RoomDTO buildRoomDTO(Long id, RoomType type, int capacity, String price) {
        RoomDTO dto = new RoomDTO();
        dto.setId(id);
        dto.setRoomNumber("10" + id);
        dto.setType(type);
        dto.setCapacity(capacity);
        dto.setPricePerNight(new BigDecimal(price));
        return dto;
    }

    @Test
    @DisplayName("findAvailableRooms filtrerar bort rum med för liten kapacitet")
    void findAvailableRooms_filtersOnCapacity() {
        RoomDTO dbl   = buildRoomDTO(2L, RoomType.DOUBLE, 2, "1200");
        RoomDTO large = buildRoomDTO(3L, RoomType.DOUBLE, 4, "1800");
        when(roomService.findAvailableByDates(IN, OUT, 2))
                .thenReturn(List.of(dbl, large));

        List<RoomDTO> result = searchService.findAvailableRooms(IN, OUT, 2);

        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(r -> r.getCapacity() < 2);
    }

    @Test
    @DisplayName("findAvailableRooms returnerar tom lista om inga rum matchar")
    void findAvailableRooms_noMatch_returnsEmpty() {
        when(roomService.findAvailableByDates(IN, OUT, 4))
                .thenReturn(List.of());

        List<RoomDTO> result = searchService.findAvailableRooms(IN, OUT, 4);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAvailableRooms returnerar alla rum när kapacitet räcker")
    void findAvailableRooms_allMatch_returnsAll() {
        RoomDTO r1 = buildRoomDTO(1L, RoomType.DOUBLE, 2, "1200");
        RoomDTO r2 = buildRoomDTO(2L, RoomType.DOUBLE, 4, "1800");
        when(roomService.findAvailableByDates(IN, OUT, 1))
                .thenReturn(List.of(r1, r2));

        List<RoomDTO> result = searchService.findAvailableRooms(IN, OUT, 1);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findAvailableRooms returnerar tom lista om inga rum är lediga")
    void findAvailableRooms_noAvailableRooms_returnsEmpty() {
        when(roomService.findAvailableByDates(IN, OUT, 1))
                .thenReturn(List.of());

        List<RoomDTO> result = searchService.findAvailableRooms(IN, OUT, 1);

        assertThat(result).isEmpty();
    }
}