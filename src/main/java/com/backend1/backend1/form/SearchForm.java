package com.backend1.backend1.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class SearchForm {

    @NotNull(message = "Incheckning är obligatorisk")
    private LocalDate checkIn;

    @NotNull(message = "Utcheckning är obligatorisk")
    private LocalDate checkOut;

    @Min(value = 1, message = "Minst 1 gäst krävs")
    private int numberOfGuests = 1;

}
