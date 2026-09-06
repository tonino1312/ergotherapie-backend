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

## Pendiente de documentar (siguiente en la lista)

5. `GET /api/servicios/{id}`
6. `POST /api/servicios`
7. `PUT /api/servicios/{id}`
8. `DELETE /api/servicios/{id}`
9. `GET /api/pacientes`
10. `GET /api/pacientes/{id}`
11. `POST /api/pacientes`
12. `PUT /api/pacientes/{id}`
13. `DELETE /api/pacientes/{id}`
