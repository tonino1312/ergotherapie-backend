package com.elena.proyectoelena.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Idioma idioma;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos = 60;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Servicio(String nombre, String descripcion, Idioma idioma, int duracionMinutos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idioma = idioma;
        this.duracionMinutos = duracionMinutos;
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
