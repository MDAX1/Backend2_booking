package com.backend1.backend1.config;

import com.backend1.backend1.model.Booking;
import com.backend1.backend1.model.Customer;
import com.backend1.backend1.model.Room;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.CustomerRepository;
import com.backend1.backend1.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    @PostConstruct
    @Transactional
    public void init() {
        if (roomRepository.count() > 0) return;

        List<Room> rooms = roomRepository.saveAll(List.of(
            room("101", RoomType.SINGLE, 0, "895"),
            room("102", RoomType.SINGLE, 0, "895"),
            room("103", RoomType.SINGLE, 0, "895"),
            room("104", RoomType.SINGLE, 0, "950"),
            room("201", RoomType.DOUBLE, 0, "1250"),
            room("202", RoomType.DOUBLE, 0, "1250"),
            room("203", RoomType.DOUBLE, 1, "1450"),
            room("204", RoomType.DOUBLE, 1, "1450"),
            room("205", RoomType.DOUBLE, 2, "1650"),
            room("301", RoomType.DOUBLE, 0, "1600"),
            room("302", RoomType.DOUBLE, 1, "1850"),
            room("303", RoomType.DOUBLE, 2, "2100")
        ));

        List<Customer> customers = customerRepository.saveAll(List.of(
            customer("Anna",    "Lindström",   "anna.lindstrom@gmail.com",      "070-123 45 67", "Storgatan 1, Stockholm"),
            customer("Erik",    "Johansson",   "erik.johansson@outlook.com",    "073-234 56 78", "Kungsgatan 12, Göteborg"),
            customer("Maria",   "Svensson",    "maria.svensson@hotmail.com",    "076-345 67 89", "Drottninggatan 5, Malmö"),
            customer("Lars",    "Nilsson",     "lars.nilsson@gmail.com",        "070-456 78 90", "Vasagatan 8, Uppsala"),
            customer("Karin",   "Eriksson",    "karin.eriksson@gmail.com",      "073-567 89 01", "Järnvägsgatan 3, Örebro"),
            customer("Johan",   "Persson",     "johan.persson@outlook.com",     "076-678 90 12", "Kyrkogatan 7, Linköping"),
            customer("Sara",    "Gustafsson",  "sara.gustafsson@gmail.com",     "070-789 01 23", "Parkgatan 14, Västerås"),
            customer("Anders",  "Karlsson",    "anders.karlsson@hotmail.com",   "073-890 12 34", "Torggatan 2, Helsingborg"),
            customer("Lena",    "Magnusson",   "lena.magnusson@gmail.com",      "076-901 23 45", "Linnégatan 9, Norrköping"),
            customer("Mikael",  "Olsson",      "mikael.olsson@outlook.com",     "070-012 34 56", "Bergsgatan 6, Lund")
        ));

        LocalDate today = LocalDate.now();

        bookingRepository.saveAll(List.of(
            // Past bookings
            booking(customers.get(0), rooms.get(4),  today.minusDays(47), today.minusDays(43), 2),
            booking(customers.get(1), rooms.get(0),  today.minusDays(37), today.minusDays(35), 1),
            booking(customers.get(2), rooms.get(11), today.minusDays(26), today.minusDays(22), 4),
            booking(customers.get(3), rooms.get(5),  today.minusDays(17), today.minusDays(12), 2),
            booking(customers.get(4), rooms.get(1),  today.minusDays(7),  today.minusDays(4),  1),
            booking(customers.get(8), rooms.get(8),  today.minusDays(20), today.minusDays(16), 3),
            booking(customers.get(9), rooms.get(2),  today.minusDays(10), today.minusDays(8),  1),

            // Current bookings
            booking(customers.get(5), rooms.get(9),  today.minusDays(2),  today.plusDays(3),   2),
            booking(customers.get(6), rooms.get(6),  today,               today.plusDays(6),   3),

            // Upcoming bookings
            booking(customers.get(7), rooms.get(1),  today.plusDays(5),   today.plusDays(9),   1),
            booking(customers.get(0), rooms.get(10), today.plusDays(14),  today.plusDays(19),  2),
            booking(customers.get(1), rooms.get(8),  today.plusDays(20),  today.plusDays(25),  3),
            booking(customers.get(2), rooms.get(3),  today.plusDays(30),  today.plusDays(32),  1),
            booking(customers.get(3), rooms.get(11), today.plusDays(45),  today.plusDays(51),  4)
        ));
    }

    private Room room(String number, RoomType type, int extraBeds, String price) {
        Room r = new Room();
        r.setRoomNumber(number);
        r.setType(type);
        r.setExtraBeds(extraBeds);
        r.setPricePerNight(new BigDecimal(price));
        return r;
    }

    private Customer customer(String firstName, String lastName, String email, String phone, String address) {
        Customer c = new Customer();
        c.setFirstName(firstName);
        c.setLastName(lastName);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        return c;
    }

    private Booking booking(Customer customer, Room room, LocalDate checkIn, LocalDate checkOut, int guests) {
        Booking b = new Booking();
        b.setCustomer(customer);
        b.setRoom(room);
        b.setCheckIn(checkIn);
        b.setCheckOut(checkOut);
        b.setNumberOfGuests(guests);
        return b;
    }
}
