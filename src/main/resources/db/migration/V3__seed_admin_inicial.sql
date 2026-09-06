-- Usuario administrador inicial. Contraseña temporal: CambiaEstaClave123!
-- IMPORTANTE: cambiar esta contraseña inmediatamente tras el primer login.
INSERT INTO usuarios (nombre, email, password_hash, rol, activo)
VALUES ('Administrador', 'admin@ergotherapie.local', '$2a$10$m.P3UWim8PNtC467iLrVsuIaphaDJKbgkIZ4F4AC8zyHKet8pGGOK', 'ADMIN', TRUE);
