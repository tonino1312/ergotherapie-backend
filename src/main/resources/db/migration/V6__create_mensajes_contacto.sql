CREATE TABLE mensajes_contacto (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    asunto      VARCHAR(200) NOT NULL,
    mensaje     TEXT,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'NUEVO',
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_mensajes_contacto_estado CHECK (estado IN ('NUEVO', 'LEIDO', 'RESPONDIDO', 'DESCARTADO'))
);

CREATE INDEX idx_mensajes_contacto_estado ON mensajes_contacto (estado);
CREATE INDEX idx_mensajes_contacto_created_at ON mensajes_contacto (created_at);
