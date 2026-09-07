package com.backend1.backend1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int extraBeds = 0;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    public int getCapacity() {
        if (type == RoomType.SINGLE) return 1;
        return 2 + extraBeds;
    }
}
