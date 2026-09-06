package com.elena.proyectoelena.mapper;

import com.elena.proyectoelena.dto.cita.CitaResponse;
import com.elena.proyectoelena.model.Cita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CitaMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNombreCompleto", expression = "java(cita.getPaciente().getNombre() + \" \" + cita.getPaciente().getApellidos())")
    @Mapping(target = "terapeutaId", source = "terapeuta.id")
    @Mapping(target = "terapeutaNombre", source = "terapeuta.nombre")
    @Mapping(target = "servicioId", source = "servicio.id")
    @Mapping(target = "servicioNombre", source = "servicio.nombre")
    CitaResponse toResponse(Cita cita);
}
