package com.elena.proyectoelena.repository;

import com.elena.proyectoelena.model.Cita;
import com.elena.proyectoelena.model.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    Page<Cita> findByTerapeuta_Id(Long terapeutaId, Pageable pageable);

    List<Cita> findByTerapeuta_IdAndFechaHoraBetweenAndEstadoNot(
            Long terapeutaId, LocalDateTime desde, LocalDateTime hasta, EstadoCita estadoExcluido);
}
