package com.elena.proyectoelena.repository;

import com.elena.proyectoelena.model.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Page<Servicio> findByActivoTrue(Pageable pageable);
}
