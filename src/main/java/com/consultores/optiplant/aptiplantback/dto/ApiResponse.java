package com.consultores.optiplant.aptiplantback.dto;

import java.time.LocalDateTime;

/**
 * DTO genérico para respuestas de API, que incluye un indicador de éxito, un mensaje, datos opcionales y una marca de tiempo.
 *
 * @param <T> El tipo de los datos incluidos en la respuesta.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
