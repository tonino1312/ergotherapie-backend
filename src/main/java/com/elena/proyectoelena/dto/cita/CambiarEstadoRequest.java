package com.elena.proyectoelena.dto.cita;

import com.elena.proyectoelena.model.EstadoCita;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoCita estado
) {
}
