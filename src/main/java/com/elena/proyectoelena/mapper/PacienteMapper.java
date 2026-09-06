package com.elena.proyectoelena.mapper;

import com.elena.proyectoelena.dto.paciente.PacienteResponse;
import com.elena.proyectoelena.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "terapeutaId", source = "terapeuta.id")
    @Mapping(target = "terapeutaNombre", source = "terapeuta.nombre")
    PacienteResponse toResponse(Paciente paciente);
}
