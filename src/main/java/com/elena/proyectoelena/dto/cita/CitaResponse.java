package com.elena.proyectoelena.dto.cita;

import java.time.Instant;
import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombreCompleto,
        Long terapeutaId,
        String terapeutaNombre,
        Long servicioId,
        String servicioNombre,
        LocalDateTime fechaHora,
        int duracionMinutos,
        String estado,
        String notas,
        Instant createdAt,
        Instant updatedAt
) {
}
