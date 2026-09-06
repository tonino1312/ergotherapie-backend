package com.elena.proyectoelena.dto.contacto;

import java.time.Instant;

public record MensajeContactoResponse(
        Long id,
        String nombre,
        String email,
        String asunto,
        String mensaje,
        String estado,
        Instant createdAt,
        Instant updatedAt
) {
}
