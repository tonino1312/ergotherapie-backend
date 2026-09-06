# Documentación técnica — API Proyecto Elena

Registro de cada endpoint a medida que se implementa y se prueba manualmente (Swagger UI en `http://localhost:8080/swagger-ui/index.html`). Los JSON de ejemplo usados en las pruebas están en `api-examples/`, numerados en el mismo orden que aquí.

Estado del proyecto en el momento de este documento: rama `feature/DesarrolloERGO`, dominios implementados: Auth, Pacientes, Servicios.

---

## 1. `GET /api/health`

- **Para qué sirve**: comprobar que la aplicación está levantada y respondiendo.
- **Acceso**: público, sin token.
- **Base de datos**: no accede a BD.
- **Respuesta (200)**:
```json
{ "status": "UP", "timestamp": "2026-09-06T18:02:33.979197200Z" }
```
- **Probado**: ✅ OK.

---

## 2. `POST /api/auth/login`

- **Para qué sirve**: autenticar un usuario (terapeuta o admin) y devolver un JWT para usar en el resto de endpoints.
- **Acceso**: público.
- **Base de datos**: `SELECT` sobre `usuarios` por `email`; verifica la contraseña contra el hash bcrypt guardado (no se compara en texto plano).
- **Request** (`api-examples/02-auth-login.json`):
```json
{
  "email": "admin@ergotherapie.local",
  "password": "CambiaEstaClave123!"
}
```
- **Respuesta (200)**:
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "nombre": "Administrador",
  "email": "admin@ergotherapie.local",
  "rol": "ADMIN"
}
```
- **Errores esperados**: `401` si el email o la contraseña no son correctos.
- **Probado**: ✅ OK.

---

## 3. `POST /api/auth/register`

- **Para qué sirve**: dar de alta un nuevo usuario del sistema (terapeuta o admin). No es un registro público: solo un `ADMIN` autenticado puede crear usuarios.
- **Acceso**: requiere token con rol `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
- **Base de datos**: `SELECT` para comprobar que el email no exista ya; `INSERT` en `usuarios` con la contraseña cifrada (bcrypt).
- **Request** (`api-examples/03-auth-register.json`):
```json
{
  "nombre": "Maria Fernandez",
  "email": "maria.fernandez@ergotherapie.local",
  "password": "ClaveSegura123!",
  "rol": "TERAPEUTA"
}
```
- **Respuesta (201)**: sin body.
- **Errores esperados y probados**:
  - `409 Conflict` al repetir la misma petición (email duplicado)
  - `403 Forbidden` al llamarlo sin token o con un token que no sea de `ADMIN`
- **Probado**: ✅ OK, incluidos los dos casos de error.

---

## 4. `GET /api/servicios`

- **Para qué sirve**: listar el catálogo de servicios activos, paginado.
- **Acceso**: público, sin token.
- **Base de datos**: `SELECT` sobre `servicios WHERE activo = true`, paginado. Admite `page`, `size`, `sort` como parámetros de query opcionales.
- **Respuesta (200)** de ejemplo:
```json
{
  "content": [
    { "id": 1, "nombre": "Terapia e Integracion Sensorial", "descripcion": "Sesion individual", "idioma": "ESPANOL", "duracionMinutos": 45, "activo": true }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```
- **Bug encontrado y corregido durante las pruebas**: Swagger UI rellena el campo `sort` con el placeholder `string` por defecto. Si se ejecuta sin borrarlo, Spring Data intenta ordenar por una columna llamada `"string"` (que no existe) y lanzaba `InvalidDataAccessApiUsageException`, que el `GlobalExceptionHandler` capturaba con el handler genérico de `Exception` → devolvía **500 Internal Server Error**, incorrecto para un error de entrada del cliente.
  - **Fix**: se añadió un `@ExceptionHandler(InvalidDataAccessApiUsageException.class)` específico en `GlobalExceptionHandler` que ahora devuelve **400 Bad Request** con el mensaje `"Parámetro de consulta inválido (revisa 'sort')"`.
  - **Lección**: al probar por Swagger, hay que borrar el campo `sort` si no se quiere ordenar por algo concreto, no dejarlo con el valor de ejemplo.
- **Probado**: ✅ OK — listado normal, y el caso de `sort` inválido ahora devuelve 400 en vez de 500.

---

## 5. `GET /api/servicios/{id}`

- **Para qué sirve**: obtener un servicio concreto por id.
- **Acceso**: público.
- **Base de datos**: `SELECT ... WHERE id = ? AND activo = true`.
- **Probado**: ✅ id existente → 200 con los datos; id inexistente (`999`) → 404 con mensaje descriptivo.

---

## 6. `POST /api/servicios`

- **Para qué sirve**: crear un nuevo servicio en el catálogo.
- **Acceso**: solo `ADMIN`.
- **Base de datos**: `INSERT` en `servicios`.
- **Request** de ejemplo:
```json
{ "nombre": "Grupo de Juego", "descripcion": "Sesion grupal para desarrollo psicomotor", "idioma": "ALEMAN", "duracionMinutos": 90 }
```
- **Probado**: ✅ con token ADMIN → 201 y el recurso creado; sin token → 403; con `nombre` vacío → 400 con el mensaje de validación (`"nombre: El nombre es obligatorio"`).

---

## 7. `PUT /api/servicios/{id}`

- **Para qué sirve**: actualizar un servicio existente.
- **Acceso**: solo `ADMIN`.
- **Base de datos**: `SELECT` + `UPDATE` (vía dirty checking de JPA, dentro de la transacción del servicio).
- **Probado**: ✅ 200 con los datos actualizados.

---

## 8. `DELETE /api/servicios/{id}`

- **Para qué sirve**: dar de baja un servicio (borrado lógico: `activo = false`, la fila no se borra físicamente).
- **Acceso**: solo `ADMIN`.
- **Base de datos**: `UPDATE servicios SET activo = false WHERE id = ?`.
- **Probado**: ✅ 204 sin body; verificado que tras el borrado ya no aparece en `GET /api/servicios` y que `GET /api/servicios/{id}` de ese mismo id da 404 (queda "invisible" para la API aunque siga en la tabla).

---

## 9. `GET /api/pacientes`

- **Para qué sirve**: listar pacientes, paginado.
- **Acceso**: requiere token. Si el usuario es `TERAPEUTA`, solo ve los pacientes donde él es el `terapeuta` asignado; si es `ADMIN`, ve todos.
- **Base de datos**: `SELECT` sobre `pacientes WHERE activo = true` (y `AND terapeuta_id = ?` si no es admin).
- **Probado**: ✅ como `ADMIN` ve todos los pacientes; sin token → 403. Además se probó específicamente que un `TERAPEUTA` que no tiene pacientes asignados recibe una lista vacía, no los de otros (ver nota de seguridad al final del documento).

---

## 10. `GET /api/pacientes/{id}`

- **Para qué sirve**: obtener un paciente concreto.
- **Acceso**: requiere token; un `TERAPEUTA` solo puede ver los suyos (si no, `403`); `ADMIN` puede ver cualquiera.
- **Base de datos**: `SELECT ... WHERE id = ? AND activo = true`, luego se verifica en código que el `terapeuta_id` coincida con el usuario autenticado (o que sea `ADMIN`).
- **Probado**: ✅ id existente y propio → 200; id inexistente → 404; id de un paciente ajeno (con un `TERAPEUTA` sin permiso) → 403.

---

## 11. `POST /api/pacientes`

- **Para qué sirve**: dar de alta un paciente nuevo.
- **Acceso**: requiere token (cualquier rol). El paciente se asigna automáticamente al usuario autenticado como terapeuta, salvo que sea `ADMIN` y especifique `terapeutaId` en el body.
- **Base de datos**: `INSERT` en `pacientes`.
- **Request** de ejemplo:
```json
{ "nombre": "Pablo", "apellidos": "Martinez Diaz", "fechaNacimiento": "2018-04-12", "email": "pablo.padres@example.com", "telefono": "600222333" }
```
- **Probado**: ✅ 201 con el paciente creado y `terapeutaId` asignado correctamente al usuario que hizo la petición.

---

## 12. `PUT /api/pacientes/{id}`

- **Para qué sirve**: actualizar los datos de un paciente (incluidas las notas clínicas).
- **Acceso**: requiere token, mismo control de acceso que el resto (propio terapeuta o `ADMIN`).
- **Base de datos**: `SELECT` + `UPDATE` (dirty checking).
- **Probado**: ✅ 200 con los datos actualizados, incluida la escritura de `notasClinicas`.

---

## 13. `DELETE /api/pacientes/{id}`

- **Para qué sirve**: dar de baja a un paciente (borrado lógico).
- **Acceso**: requiere token, mismo control de acceso.
- **Base de datos**: `UPDATE pacientes SET activo = false WHERE id = ?`.
- **Probado**: ✅ 204, y confirmado que desaparece del listado tras el borrado.

---

## Nota de seguridad verificada (importante)

Se probó explícitamente el caso más crítico de la aplicación: un usuario `TERAPEUTA` (`maria.fernandez@ergotherapie.local`, creado en la prueba del endpoint 3) **no puede ver ni acceder a pacientes de otro terapeuta**, ni realizar acciones de `ADMIN`:
- `GET /api/pacientes` autenticado como esa terapeuta → lista vacía (el único paciente existente es del `ADMIN`)
- `GET /api/pacientes/1` (paciente del `ADMIN`) → `403 Forbidden`
- `POST /api/servicios` (acción de solo-admin) → `403 Forbidden`

Esto confirma que el aislamiento de datos por terapeuta y el control de acceso por rol funcionan como se diseñaron.

## Resumen: 13/13 endpoints probados y documentados ✅

---

# Dominio: Citas (agenda)

Vincula `Paciente` + `Terapeuta` (`Usuario`) + `Servicio`, con fecha/hora, duración, estado (`PROGRAMADA`, `COMPLETADA`, `CANCELADA`, `NO_ASISTIO`) y notas. Es el dominio más complejo del backend porque implementa **detección de solapamiento de horario**: un terapeuta no puede tener dos citas activas (no canceladas) que se pisen en el tiempo.

Cómo funciona la comprobación (`CitaService.verificarSinSolapamiento`): al crear o mover una cita, se cargan todas las citas de ese terapeuta ese mismo día (excluyendo las `CANCELADA`) y se comprueba en memoria si el rango `[fechaHora, fechaHora + duración)` se cruza con el de alguna existente. Si hay cruce, `409 Conflict`.

## 14. `POST /api/citas`

- **Para qué sirve**: reservar una cita nueva.
- **Acceso**: requiere token. El `terapeuta` de la cita es el usuario autenticado, salvo que sea `ADMIN` y especifique `terapeutaId`. El `paciente` debe pertenecer a ese terapeuta (o el usuario ser `ADMIN`).
- **Base de datos**: `SELECT` de paciente/servicio/terapeuta para validarlos, `SELECT` de citas del día para comprobar solapamiento, `INSERT` en `citas`.
- **Request** (`api-examples/14-citas-create.json`):
```json
{ "pacienteId": 3, "servicioId": 3, "fechaHora": "2026-09-10T10:00:00", "notas": "Primera valoracion" }
```
  - `duracionMinutos` es opcional: si no se manda, se usa la duración por defecto del `Servicio`.
- **Probado**: ✅ 201 con duración heredada del servicio (45 min); `404` con `pacienteId` inexistente; `400` sin `fechaHora`; `403` sin token.
- **Probado (regla de negocio central)**: ✅ crear una segunda cita que se solapa con la primera (mismo terapeuta, dentro del rango horario) → **409 Conflict** ("El terapeuta ya tiene una cita en ese horario"); crear una cita justo después de que termine la anterior → **201**, sin conflicto.

## 15. `GET /api/citas`

- **Para qué sirve**: listar citas, paginado, ordenado por `fechaHora` por defecto.
- **Acceso**: requiere token. `TERAPEUTA` solo ve las suyas; `ADMIN` ve todas.
- **Base de datos**: `SELECT` sobre `citas` (filtrado por `terapeuta_id` si no es admin).
- **Probado**: ✅ 200 con las citas creadas.

## 16. `GET /api/citas/{id}`

- **Para qué sirve**: obtener una cita concreta.
- **Acceso**: requiere token, con el mismo control de acceso (propio terapeuta o `ADMIN`).
- **Base de datos**: `SELECT ... WHERE id = ?`.
- **Probado**: ✅ 200 con los datos completos (paciente, terapeuta y servicio "expandidos" con nombre, no solo el id).

## 17. `PUT /api/citas/{id}`

- **Para qué sirve**: reprogramar una cita (cambiar fecha/hora, paciente, servicio o notas). Vuelve a comprobar el solapamiento, excluyendo la propia cita que se está editando.
- **Acceso**: mismo control que el resto.
- **Base de datos**: `SELECT` + `UPDATE` (dirty checking), más la consulta de solapamiento.
- **Request** (`api-examples/17-citas-update.json`):
```json
{ "pacienteId": 3, "servicioId": 3, "fechaHora": "2026-09-10T09:00:00", "notas": "Movida de horario" }
```
- **Probado**: ✅ 200, cita movida de las 10:00 a las 09:00 sin problema (no colisiona consigo misma).

## 18. `PATCH /api/citas/{id}/estado`

- **Para qué sirve**: cambiar solo el estado de una cita (marcarla como completada, no asistida, etc.) sin tocar el resto de datos. Endpoint separado de `PUT` a propósito, porque cambiar el estado es una acción distinta a reprogramar.
- **Acceso**: mismo control que el resto.
- **Base de datos**: `UPDATE citas SET estado = ?`.
- **Request** (`api-examples/18-citas-cambiar-estado.json`):
```json
{ "estado": "COMPLETADA" }
```
- **Probado**: ✅ 200, `estado` pasa de `PROGRAMADA` a `COMPLETADA`.

## 19. `DELETE /api/citas/{id}`

- **Para qué sirve**: cancelar una cita. No es un borrado físico: pone `estado = CANCELADA`, conservando el historial (nunca se pierde el registro de que existió esa cita).
- **Acceso**: mismo control que el resto.
- **Base de datos**: `UPDATE citas SET estado = 'CANCELADA'`.
- **Probado**: ✅ 204; verificado con `GET` posterior que el estado queda en `CANCELADA`.
- **Probado (detalle de negocio)**: ✅ una vez cancelada, su hueco horario **vuelve a estar disponible** — se pudo crear una cita nueva exactamente a la misma hora que la cancelada, confirmando que la comprobación de solapamiento excluye correctamente las citas `CANCELADA`.

## Resumen: 19/19 endpoints probados y documentados ✅

Próximos dominios sugeridos: formulario de contacto público (leads) y/o cursos con inscripción — ver conversación para el orden acordado.
