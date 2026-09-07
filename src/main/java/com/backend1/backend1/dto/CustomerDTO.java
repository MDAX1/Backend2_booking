package com.backend1.backend1.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private long bookingCount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
