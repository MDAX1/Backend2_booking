package com.backend1.backend1.form;

import com.backend1.backend1.model.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomForm {
    private Long id;

    @NotBlank
    private String roomNumber;

    @NotNull
    private RoomType type;

    @Min(0)
    @Max(2)
    private int extraBeds;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal pricePerNight;
}
