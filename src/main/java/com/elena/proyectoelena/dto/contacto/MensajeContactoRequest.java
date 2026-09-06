package com.elena.proyectoelena.dto.contacto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensajeContactoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 200, message = "El asunto no puede superar los 200 caracteres")
        String asunto,

        @NotBlank(message = "El nombre del paciente es obligatorio")
        @Size(max = 150, message = "El nombre del paciente no puede superar los 150 caracteres")
        String nombrePaciente,

        @NotNull(message = "La edad del paciente es obligatoria")
        @Min(value = 0, message = "La edad del paciente no puede ser negativa")
        @Max(value = 120, message = "La edad del paciente no es válida")
        Integer edadPaciente,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        @Size(max = 3000, message = "El motivo de consulta no puede superar los 3000 caracteres")
        String motivoConsulta,

        @Size(max = 3000, message = "El tratamiento previo no puede superar los 3000 caracteres")
        String tratamientoPrevio,

        @Size(max = 5000, message = "El mensaje no puede superar los 5000 caracteres")
        String mensaje
) {
}
