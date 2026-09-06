package com.elena.proyectoelena.dto.paciente;

import java.time.Instant;
import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String email,
        String telefono,
        String direccion,
        String notasClinicas,
        Long terapeutaId,
        String terapeutaNombre,
        boolean activo,
        Instant createdAt,
        Instant updatedAt
) {
}
