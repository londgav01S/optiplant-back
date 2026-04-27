package com.consultores.optiplant.aptiplantback.dto;

public record AuthResponse(
    String token,
    AuthUserResponse user
) {
}

