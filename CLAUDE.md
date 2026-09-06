# Proyecto Elena

Backend con **Java 21 + Spring Boot 4.1.1** para la gestión de una consulta de terapia ocupacional (ergotherapie).

## Stack
- Java 21 LTS
- Spring Boot 4.1.1 + Spring Security 7 (JWT, sin sesiones)
- Maven (`./mvnw` incluido, no requiere Maven instalado localmente)
- PostgreSQL — en `dev` se levanta automáticamente vía `compose.yaml` (Spring Boot Docker Compose support), requiere Docker Desktop corriendo
- Flyway para migraciones versionadas (`src/main/resources/db/migration`) — `ddl-auto` siempre en `validate`, el esquema lo gestiona Flyway, nunca Hibernate
- springdoc-openapi — Swagger UI en `/swagger-ui/index.html`, spec en `/v3/api-docs`
- Spring Data JPA + Bean Validation + MapStruct (mapeo entidad↔DTO) + Lombok

## Estructura de paquetes (`com.elena.proyectoelena`)
- `controller` — endpoints REST, sin lógica de negocio
- `service` — lógica de negocio (`@Transactional`)
- `repository` — interfaces Spring Data JPA
- `model` — entidades JPA
- `dto/<dominio>` — objetos de entrada/salida de la API (nunca exponer entidades directamente)
- `mapper` — interfaces MapStruct (entidad → DTO)
- `security` — `JwtService`, `JwtAuthenticationFilter`, `UserPrincipal`, `CustomUserDetailsService`
- `exception` — excepciones custom + `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `config` — `SecurityConfig`, `OpenApiConfig`

## Dominio implementado
- **Auth** (`Usuario`, roles `ADMIN`/`TERAPEUTA`): login JWT (`POST /api/auth/login`, público) y alta de usuarios (`POST /api/auth/register`, solo `ADMIN`)
- **Pacientes** (`Paciente`, ligado a un `Usuario` terapeuta): CRUD en `/api/pacientes`, protegido con JWT. Un `TERAPEUTA` solo ve/edita sus propios pacientes; un `ADMIN` ve todos. Borrado lógico (`activo=false`), no físico.
- **Servicios** (`Servicio`, catálogo por idioma): CRUD en `/api/servicios`. Lectura pública (`GET`), escritura solo `ADMIN`. Borrado lógico.
- **Citas** (`Cita`, vincula Paciente + Terapeuta + Servicio): CRUD + cambio de estado en `/api/citas`, protegido con JWT, mismo aislamiento por terapeuta que Pacientes. Incluye **detección de solapamiento de horario** (`CitaService.verificarSinSolapamiento`): no se puede crear/mover una cita si choca con otra cita activa del mismo terapeuta ese día → `409 Conflict`. Cancelar (`DELETE`) pone `estado=CANCELADA` (no borra físicamente) y libera el hueco horario.
- Usuario admin sembrado en `V3__seed_admin_inicial.sql`: `admin@ergotherapie.local` / `CambiaEstaClave123!` — **cambiar esta contraseña de inmediato**, es solo para arrancar el sistema.
- **19 endpoints en total**, todos probados y documentados en `docs/API.md` (con JSON de ejemplo en `api-examples/`).

## Configuración
- `application.yml` con perfiles `dev`/`prod` (activo por defecto: `dev`)
- Credenciales de BD y JWT vía variables de entorno (`DB_USERNAME`, `DB_PASSWORD`, `DB_URL`, `JWT_SECRET`) — nunca hardcodeadas. En `dev` hay defaults locales solo para desarrollo.
- Para arrancar en local: `./mvnw spring-boot:run` (con Docker Desktop abierto) — Postgres se crea solo la primera vez.

## Convenciones de código
- Seguir las convenciones estándar de Java (nombres de paquetes en minúsculas, clases en PascalCase, métodos/variables en camelCase).
- Organizar el código por capas: `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `security`, `exception`.
- Usar DTOs (records) para las respuestas de la API, no exponer entidades JPA directamente.
- Inyección de dependencias por constructor (Lombok `@RequiredArgsConstructor`), no por campo.
- Manejo de errores centralizado con `@ControllerAdvice` / `@ExceptionHandler`.
- Validación de entrada con `jakarta.validation` (`@Valid`, `@NotNull`, etc.) en los DTOs de entrada.
- Cambios de esquema de BD siempre vía nueva migración Flyway (`V{n}__descripcion.sql`), nunca editando una ya aplicada ni con `ddl-auto`.

## Buenas prácticas
- Tests con JUnit 5 y Mockito para lógica de negocio; tests de integración con `@SpringBootTest` cuando aporte valor real (considerar Testcontainers para tests contra Postgres real).
- No dejar código muerto ni comentarios explicando "qué hace" el código (el código debe ser autoexplicativo); comentarios solo para explicar decisiones no obvias.
- Configuración sensible (credenciales, claves) en variables de entorno, nunca hardcodeada ni commiteada.

## Pendiente / próximos pasos
- Formulario de contacto público (leads) y/o Cursos con inscripción — inspirado en ergotherapie-kids.de, ver `docs/API.md` para el contexto.
- Forzar cambio de contraseña del admin sembrado en el primer login.
- Tests automatizados (unitarios de servicios + al menos un test de integración con Testcontainers).
- Ver sección "Consejos para producción" más abajo (AWS, config, cifrado de datos clínicos).

## Notas
- Este archivo se irá actualizando a medida que el proyecto avance (arquitectura, base de datos elegida, decisiones importantes).
