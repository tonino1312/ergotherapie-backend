-- Cuentas publicas de visitantes/clientes, separadas de "usuarios" (staff).
-- No tienen relacion con Paciente/Cita todavia: es la base para funcionalidad futura.
CREATE TABLE clientes (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_clientes_email UNIQUE (email)
);

CREATE INDEX idx_clientes_email ON clientes (email);
