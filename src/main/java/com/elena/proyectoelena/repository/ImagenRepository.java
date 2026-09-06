package com.elena.proyectoelena.repository;

import com.elena.proyectoelena.model.CategoriaImagen;
import com.elena.proyectoelena.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenRepository extends JpaRepository<Imagen, Long> {

    List<Imagen> findByCategoriaAndActivoTrueOrderByOrdenAsc(CategoriaImagen categoria);
}
