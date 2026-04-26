package com.consultores.optiplant.aptiplantback.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    String email,
    String nombreCompleto
) {
}

