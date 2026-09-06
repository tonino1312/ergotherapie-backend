-- El formulario de contacto ahora recoge datos del caso/paciente, no solo un mensaje generico.
-- Todo nullable porque los mensajes ya existentes no tienen estos datos.
ALTER TABLE mensajes_contacto
    ADD COLUMN telefono           VARCHAR(30),
    ADD COLUMN nombre_paciente    VARCHAR(150),
    ADD COLUMN edad_paciente      INT,
    ADD COLUMN motivo_consulta    TEXT,
    ADD COLUMN tratamiento_previo TEXT;
