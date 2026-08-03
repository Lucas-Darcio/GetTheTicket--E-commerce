package com.gettheticket.backend.user.dto;

public record UserResponseDTO(
        Long id, String name, String email, String role
) {}