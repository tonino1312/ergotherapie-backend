package com.elena.proyectoelena.dto.contacto;

import java.time.Instant;

public record MensajeContactoResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String asunto,
        String nombrePaciente,
        Integer edadPaciente,
        String motivoConsulta,
        String tratamientoPrevio,
        String mensaje,
        String estado,
        Instant createdAt,
        Instant updatedAt
) {
}
