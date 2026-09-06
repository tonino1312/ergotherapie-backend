CREATE TABLE citas (
    id                  BIGSERIAL PRIMARY KEY,
    paciente_id         BIGINT      NOT NULL,
    terapeuta_id        BIGINT      NOT NULL,
    servicio_id         BIGINT      NOT NULL,
    fecha_hora          TIMESTAMP   NOT NULL,
    duracion_minutos    INT         NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    notas               TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_citas_terapeuta FOREIGN KEY (terapeuta_id) REFERENCES usuarios (id),
    CONSTRAINT fk_citas_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id),
    CONSTRAINT ck_citas_estado CHECK (estado IN ('PROGRAMADA', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO'))
);

CREATE INDEX idx_citas_terapeuta_fecha ON citas (terapeuta_id, fecha_hora);
CREATE INDEX idx_citas_paciente ON citas (paciente_id);
