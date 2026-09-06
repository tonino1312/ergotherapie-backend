package com.elena.proyectoelena.dto.cita;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CitaRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El servicio es obligatorio")
        Long servicioId,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaHora,

        @Min(value = 5, message = "La duración mínima es de 5 minutos")
        Integer duracionMinutos,

        String notas,

        Long terapeutaId
) {
}
