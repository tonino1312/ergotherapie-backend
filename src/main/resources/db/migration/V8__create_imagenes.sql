-- Metadatos de imagenes subidas (hero, carrusel de inicio, servicios, etc.).
-- El archivo en si NO se guarda aqui: vive en disco (dev) o en un object storage
-- tipo S3 (prod). Esta tabla solo guarda donde encontrarlo y como usarlo.
CREATE TABLE imagenes (
    id                  BIGSERIAL PRIMARY KEY,
    categoria           VARCHAR(30)  NOT NULL,
    nombre_archivo      VARCHAR(255) NOT NULL,
    texto_alternativo   VARCHAR(255) NOT NULL,
    orden               INT          NOT NULL DEFAULT 0,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_imagenes_nombre_archivo UNIQUE (nombre_archivo),
    CONSTRAINT ck_imagenes_categoria CHECK (categoria IN ('CARRUSEL_INICIO', 'HERO', 'SERVICIOS', 'GENERAL'))
);

CREATE INDEX idx_imagenes_categoria ON imagenes (categoria, orden);
