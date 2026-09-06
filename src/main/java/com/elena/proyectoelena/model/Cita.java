package com.elena.proyectoelena.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terapeuta_id", nullable = false)
    private Usuario terapeuta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Cita(Paciente paciente, Usuario terapeuta, Servicio servicio,
                LocalDateTime fechaHora, int duracionMinutos, String notas) {
        this.paciente = paciente;
        this.terapeuta = terapeuta;
        this.servicio = servicio;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.notas = notas;
        this.estado = EstadoCita.PROGRAMADA;
    }

    public LocalDateTime getFechaFin() {
        return fechaHora.plusMinutes(duracionMinutos);
    }

    public boolean seSolapaCon(LocalDateTime otraFechaHora, int otraDuracionMinutos) {
        LocalDateTime otraFin = otraFechaHora.plusMinutes(otraDuracionMinutos);
        return fechaHora.isBefore(otraFin) && otraFechaHora.isBefore(getFechaFin());
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
