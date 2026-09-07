package com.backend1.backend1.service;

import com.backend1.backend1.dto.BookingDTO;
import com.backend1.backend1.exception.BookingConflictException;
import com.backend1.backend1.exception.BookingValidationException;
import com.backend1.backend1.model.Booking;
import com.backend1.backend1.model.Customer;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.CustomerRepository;
import com.backend1.backend1.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private BookingService bookingService;

    private final LocalDate TODAY  = LocalDate.of(2025, 6, 1);
    private final LocalDate TMRW   = LocalDate.of(2025, 6, 2);
    private final LocalDate WEEK   = LocalDate.of(2025, 6, 8);

    private Customer buildCustomer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Test");
        c.setLastName("Kund");
        return c;
    }

    private Room buildRoom(Long id, RoomType type, int extraBeds, String price) {
        Room r = new Room();
        r.setId(id);
        r.setRoomNumber("10" + id);
        r.setType(type);
        r.setExtraBeds(extraBeds);
        r.setPricePerNight(new BigDecimal(price));
        return r;
    }

    private Booking buildBooking(Long id, Customer c, Room r, LocalDate in, LocalDate out) {
        Booking b = new Booking();
        b.setId(id);
        b.setCustomer(c);
        b.setRoom(r);
        b.setCheckIn(in);
        b.setCheckOut(out);
        b.setNumberOfGuests(1);
        return b;
    }

    @Test
    @DisplayName("findAll returnerar alla bokningar som DTO-lista")
    void findAll_returnsMappedDTOs() {
        Room r = buildRoom(1L, RoomType.SINGLE, 0, "800");
        Customer c = buildCustomer(1L);
        Booking b = buildBooking(1L, c, r, TODAY, WEEK);
        when(bookingRepository.findAll()).thenReturn(List.of(b));

        List<BookingDTO> result = bookingService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRoomNumber()).isEqualTo("101");
        assertThat(result.getFirst().getNights()).isEqualTo(7);
        assertThat(result.getFirst().getTotalPrice()).isEqualByComparingTo("5600");
    }

    @Test
    @DisplayName("findById med giltigt id returnerar korrekt DTO")
    void findById_found_returnsDTO() {
        Room r = buildRoom(1L, RoomType.DOUBLE, 0, "1200");
        Customer c = buildCustomer(2L);
        Booking b = buildBooking(5L, c, r, TODAY, TMRW);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));

        BookingDTO dto = bookingService.findById(5L);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getNights()).isEqualTo(1);
        assertThat(dto.getTotalPrice()).isEqualByComparingTo("1200");
    }

    @Test
    @DisplayName("findById med okänt id kastar IllegalArgumentException")
    void findById_notFound_throwsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    //Testar att fel datum blockeras
    @Test
    @DisplayName("save kastar BookingValidationException om checkOut inte är efter checkIn")
    void save_checkOutBeforeCheckIn_throwsValidationException() {
        assertThatThrownBy(() ->
                bookingService.save(null, 1L, 1L, WEEK, TODAY, 1))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Utcheckningsdatum");
    }

    @Test
    @DisplayName("save kastar BookingValidationException om checkOut är samma dag som checkIn")
    void save_checkOutSameDay_throwsValidationException() {
        assertThatThrownBy(() ->
                bookingService.save(null, 1L, 1L, TODAY, TODAY, 1))
                .isInstanceOf(BookingValidationException.class);
    }

    // Testar att ett enkelrum inte kan bokas för 2 gäster
    @Test
    @DisplayName("save kastar BookingValidationException om gäster överstiger rumskapacitet")
    void save_tooManyGuests_throwsValidationException() {

        // Enkelrum = kapacitet 1, försöker boka 2 gäster
        Room r = buildRoom(1L, RoomType.SINGLE, 0, "800");

        Customer c = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() ->
                bookingService.save(null, 1L, 1L, TODAY, WEEK, 2))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("person");
    }

    @Test
    @DisplayName("save godkänner bokning när antal gäster är exakt rumskapacitet")
    void save_exactCapacity_succeeds() {

        // Dubbelrum utan extrasängar = kapacitet 2
        Room r = buildRoom(2L, RoomType.DOUBLE, 0, "1200");

        Customer c = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(r));

        when(bookingRepository.countByRoomIdAndCheckInBeforeAndCheckOutAfter(
                anyLong(), any(), any())).thenReturn(0L);

        assertThatNoException().isThrownBy(() ->
                bookingService.save(null, 1L, 2L, TODAY, WEEK, 2));

        verify(bookingRepository).save(any(Booking.class));
    }

    //Testar att dubbelbokningar blockeras
    @Test
    @DisplayName("save kastar BookingConflictException om rummet redan är bokat")
    void save_roomAlreadyBooked_throwsConflictException() {

        Room r = buildRoom(1L, RoomType.SINGLE, 0, "800");
        Customer c = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(r));

        // Simulera befintlig överlappande bokning
        when(bookingRepository.countByRoomIdAndCheckInBeforeAndCheckOutAfter(
                anyLong(), any(), any())).thenReturn(1L);

        assertThatThrownBy(() ->
                bookingService.save(null, 1L, 1L, TODAY, WEEK, 1))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("bokat");
    }

    @Test
    @DisplayName("save tillåter uppdatering av befintlig bokning (exkluderar sig själv ur konfliktcheck)")
    void save_updateExistingBooking_excludesSelf() {

        Room r = buildRoom(1L, RoomType.SINGLE, 0, "800");
        Customer c = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(r));

        // countByRoomIdAndCheckInBeforeAndCheckOutAfterAndIdNot ska returnera 0
        when(bookingRepository.countByRoomIdAndCheckInBeforeAndCheckOutAfterAndIdNot(
                anyLong(), any(), any(), eq(10L))).thenReturn(0L);

        assertThatNoException().isThrownBy(() ->
                bookingService.save(10L, 1L, 1L, TODAY, WEEK, 1));

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("save kastar BookingValidationException om kunden inte finns")
    void save_unknownCustomer_throwsValidationException() {

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.save(null, 99L, 1L, TODAY, WEEK, 1))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Kund");
    }

    @Test
    @DisplayName("save kastar BookingValidationException om rummet inte finns")
    void save_unknownRoom_throwsValidationException() {

        Customer c = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.save(null, 1L, 99L, TODAY, WEEK, 1))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Rum");
    }

    @Test
    @DisplayName("delete anropar repository.deleteById med rätt id")
    void delete_callsDeleteById() {

        bookingService.delete(7L);

        verify(bookingRepository, times(1)).deleteById(7L);
    }

    @Test
    @DisplayName("count returnerar antalet bokningar från repository")
    void count_returnsRepositoryCount() {

        when(bookingRepository.count()).thenReturn(4L);

        assertThat(bookingService.count()).isEqualTo(4L);
    }
}
