package com.backend1.backend1.dto;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        boolean deleted
) {}