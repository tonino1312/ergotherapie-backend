CREATE TABLE pacientes (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(150) NOT NULL,
    apellidos         VARCHAR(150) NOT NULL,
    fecha_nacimiento  DATE,
    email             VARCHAR(255),
    telefono          VARCHAR(30),
    direccion         VARCHAR(255),
    notas_clinicas    TEXT,
    terapeuta_id      BIGINT       NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_pacientes_terapeuta FOREIGN KEY (terapeuta_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_pacientes_terapeuta ON pacientes (terapeuta_id);
CREATE INDEX idx_pacientes_apellidos ON pacientes (apellidos);
