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
- `security` — `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, y dos identidades que implementan `TokenPrincipal` (para que `JwtService` emita el mismo tipo de JWT a ambas sin acoplarse a ninguna): `UserPrincipal` (staff) y `ClientePrincipal` (cuentas públicas)
- `exception` — excepciones custom + `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `config` — `SecurityConfig`, `OpenApiConfig`

## Dominio implementado
- **Auth** (`Usuario`, roles `ADMIN`/`TERAPEUTA`): login JWT (`POST /api/auth/login`, público), login con Google (`POST /api/auth/google`, ver abajo) y alta de usuarios (`POST /api/auth/register`, solo `ADMIN`)
- **Pacientes** (`Paciente`, ligado a un `Usuario` terapeuta): CRUD en `/api/pacientes`, protegido con JWT. Un `TERAPEUTA` solo ve/edita sus propios pacientes; un `ADMIN` ve todos. Borrado lógico (`activo=false`), no físico.
- **Servicios** (`Servicio`, catálogo por idioma): CRUD en `/api/servicios`. Lectura pública (`GET`), escritura solo `ADMIN`. Borrado lógico.
- **Citas** (`Cita`, vincula Paciente + Terapeuta + Servicio): CRUD + cambio de estado en `/api/citas`, protegido con JWT, mismo aislamiento por terapeuta que Pacientes. Incluye **detección de solapamiento de horario** (`CitaService.verificarSinSolapamiento`): no se puede crear/mover una cita si choca con otra cita activa del mismo terapeuta ese día → `409 Conflict`. Cancelar (`DELETE`) pone `estado=CANCELADA` (no borra físicamente) y libera el hueco horario.
- **Contacto** (`MensajeContacto`): `POST /api/contacto` público (formulario de la web), resto (`GET`, `PATCH .../estado`) requiere token pero SIN aislamiento por terapeuta — bandeja compartida de todo el equipo. Flujo de estado `NUEVO` → `LEIDO` → `RESPONDIDO`/`DESCARTADO`. Sin `PUT` (el mensaje lo escribe el visitante) ni `DELETE` físico (los leads quedan como histórico).
- **Clientes** (`Cliente`, tabla y rol `CLIENTE` **totalmente separados** de `usuarios`/staff): auto-registro público (`POST /api/clientes/registro`), login (`POST /api/clientes/login`) y login con Google (`POST /api/clientes/google` — a diferencia del de staff, este SÍ auto-crea la cuenta la primera vez, ya que los visitantes pueden auto-registrarse). Sin funcionalidad especial todavía — es la base para features públicas futuras (ver historial propio, reservar citas, etc.). Un JWT de `CLIENTE` no puede acceder a ningún endpoint de staff (verificado: `403` en `/api/pacientes`), porque `CustomUserDetailsService` solo resuelve identidades contra `usuarios`.
- **Imágenes** (`Imagen`, categorías `CARRUSEL_INICIO`/`HERO`/`SERVICIOS`/`GENERAL`): `GET /api/imagenes?categoria=X` público, `POST`/`DELETE` solo `ADMIN`. El archivo **no se guarda en la BD** (mala práctica) — vive en disco (`FileStorageService`, `app.uploads.dir`, gitignored) con nombre aleatorio (UUID), servido como estático en `/uploads/**`; la tabla solo guarda metadatos. Cambiar a S3 en producción significa tocar solo `FileStorageService`, nada más.
- Usuario admin sembrado en `V3__seed_admin_inicial.sql`: `admin@ergotherapie.local` / `CambiaEstaClave123!` — **cambiar esta contraseña de inmediato**, es solo para arrancar el sistema.
- **30 endpoints en total**, todos probados y documentados en `docs/API.md` (con JSON de ejemplo en `api-examples/`).

## Login con Google (staff)
- `GoogleTokenVerifier` (`security/`) verifica el `idToken` contra las claves públicas de Google (JWKS), comprobando firma, emisor y audiencia — nunca confía en datos del cliente sin verificar.
- Solo vincula por email a un `Usuario` **ya existente** (no auto-registra). Si el email de Google no está en `usuarios`, `403`.
- Requiere `GOOGLE_CLIENT_ID` configurado (mismo Client ID que usa el frontend). Sin configurar, el endpoint falla siempre en seguro (`401`), nunca deja pasar un token sin comprobar audiencia.
- Ver `docs/API.md` (endpoint `POST /api/auth/google`) para el detalle completo.

## Notificación de login por email
- `EmailService.enviarNotificacionLogin` (`@Async`, no bloquea el login ni lo hace fallar si el envío falla): se dispara en `AuthService.login()` y `AuthService.loginWithGoogle()`, para cualquier rol (`ADMIN` o `TERAPEUTA`).
- En `dev`, el SMTP apunta a **Mailpit** (`compose.yaml`, servicio `mailpit`) — servidor SMTP falso, sin credenciales reales. Ver los correos enviados en `http://localhost:8025`.
- **Importante**: si Postgres ya estaba corriendo de una sesión anterior, Spring Boot's Docker Compose support detecta "servicios ya corriendo" y **no** arranca los servicios nuevos que se añadan a `compose.yaml` (como `mailpit`). Si añades un servicio nuevo, ejecuta `docker compose up -d` manualmente una vez.
- En `prod`, requiere `SMTP_HOST`, `SMTP_USERNAME`, `SMTP_PASSWORD` y `MAIL_FROM` (variables de entorno, sin defaults).

## Configuración
- `application.yml` con perfiles `dev`/`prod` (activo por defecto: `dev`)
- Credenciales de BD, JWT, Google y SMTP vía variables de entorno (`DB_USERNAME`, `DB_PASSWORD`, `DB_URL`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `SMTP_HOST`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `MAIL_FROM`) — nunca hardcodeadas. En `dev` hay defaults locales solo para desarrollo (`GOOGLE_CLIENT_ID` vacío, SMTP apunta a Mailpit).
- Para arrancar en local: `./mvnw spring-boot:run` (con Docker Desktop abierto) — Postgres y Mailpit se crean solos la primera vez (ver aviso arriba sobre servicios nuevos si ya había contenedores corriendo).

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

## Flujo de fotos reales
- El usuario deja archivos en `fotos-pendientes/` (carpeta gitignored en la raíz del repo, fuera de `src/`). Cuando lo haga: subirlas vía `POST /api/imagenes` (multipart, con `categoria`/`textoAlternativo`/`orden` apropiados según dónde vayan a usarse) y comprobar que aparecen bien en el frontend antes de dar el trabajo por terminado.

## Pendiente / próximos pasos
- Cursos con inscripción — inspirado en ergotherapie-kids.de, ver `docs/API.md` para el contexto.
- Rate limiting / anti-spam en `POST /api/contacto` (es público y sin protección todavía) antes de ir a producción.
- Forzar cambio de contraseña del admin sembrado en el primer login.
- Tests automatizados (unitarios de servicios + al menos un test de integración con Testcontainers).
- Ver sección "Consejos para producción" más abajo (AWS, config, cifrado de datos clínicos).

## Notas
- Este archivo se irá actualizando a medida que el proyecto avance (arquitectura, base de datos elegida, decisiones importantes).
