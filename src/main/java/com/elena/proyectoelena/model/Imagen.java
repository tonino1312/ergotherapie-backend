package com.elena.proyectoelena.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaImagen categoria;

    @Column(name = "nombre_archivo", nullable = false, unique = true)
    private String nombreArchivo;

    @Column(name = "texto_alternativo", nullable = false)
    private String textoAlternativo;

    @Column(nullable = false)
    private int orden = 0;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Imagen(CategoriaImagen categoria, String nombreArchivo, String textoAlternativo, int orden) {
        this.categoria = categoria;
        this.nombreArchivo = nombreArchivo;
        this.textoAlternativo = textoAlternativo;
        this.orden = orden;
        this.activo = true;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
