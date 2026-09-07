package com.backend1.backend1.dto;

import com.backend1.backend1.model.RoomType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDTO {
    private Long id;
    private String roomNumber;
    private RoomType type;
    private int extraBeds;
    private BigDecimal pricePerNight;
    private int capacity;
    private String typeDescription;
}
