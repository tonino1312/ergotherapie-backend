CREATE TABLE servicios (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    descripcion         TEXT,
    idioma              VARCHAR(20)  NOT NULL,
    duracion_minutos    INT          NOT NULL DEFAULT 60,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_servicios_idioma CHECK (idioma IN ('ALEMAN', 'ESPANOL', 'INGLES'))
);

CREATE INDEX idx_servicios_activo ON servicios (activo);
