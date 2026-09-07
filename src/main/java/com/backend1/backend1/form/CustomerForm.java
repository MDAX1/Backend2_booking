package com.backend1.backend1.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerForm {
    private Long id;

    @NotBlank(message = "Förnamn är obligatoriskt")
    private String firstName;

    @NotBlank(message = "Efternamn är obligatoriskt")
    private String lastName;

    private String email;
    private String phone;
    private String address;
}