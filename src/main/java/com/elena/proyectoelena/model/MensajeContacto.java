package com.elena.proyectoelena.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "mensajes_contacto")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private String email;

    private String telefono;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(name = "nombre_paciente", length = 150)
    private String nombrePaciente;

    @Column(name = "edad_paciente")
    private Integer edadPaciente;

    @Column(name = "motivo_consulta", columnDefinition = "TEXT")
    private String motivoConsulta;

    @Column(name = "tratamiento_previo", columnDefinition = "TEXT")
    private String tratamientoPrevio;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMensajeContacto estado = EstadoMensajeContacto.NUEVO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MensajeContacto(String nombre, String email, String telefono, String asunto,
                            String nombrePaciente, Integer edadPaciente, String motivoConsulta,
                            String tratamientoPrevio, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.asunto = asunto;
        this.nombrePaciente = nombrePaciente;
        this.edadPaciente = edadPaciente;
        this.motivoConsulta = motivoConsulta;
        this.tratamientoPrevio = tratamientoPrevio;
        this.mensaje = mensaje;
        this.estado = EstadoMensajeContacto.NUEVO;
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
