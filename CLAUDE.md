# Proyecto Elena

Backend con **Java 21 + Spring Boot 4.1.1**.

## Stack
- Java 21 LTS
- Spring Boot 4.1.1
- Maven (`./mvnw` incluido, no requiere Maven instalado localmente)
- PostgreSQL como base de datos (perfil `dev` apunta a `localhost:5432/proyecto_elena`)
- Spring Data JPA + Spring Web + Bean Validation
- Lombok para reducir boilerplate
- Spring DevTools en desarrollo

## Estructura de paquetes (`com.elena.proyectoelena`)
- `controller` — endpoints REST, sin lógica de negocio
- `service` — lógica de negocio
- `repository` — interfaces Spring Data JPA
- `model` — entidades JPA
- `dto` — objetos de entrada/salida de la API (nunca exponer entidades directamente)
- `exception` — excepciones custom + `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `config` — configuración de beans, seguridad, etc.

## Configuración
- `application.yml` con perfiles `dev`/`prod` (activo por defecto: `dev`)
- Credenciales de BD vía variables de entorno (`DB_USERNAME`, `DB_PASSWORD`, `DB_URL` en prod) — nunca hardcodeadas
- `ddl-auto: update` en dev por simplicidad; en prod es `validate` — cuando el esquema se estabilice, migrar a Flyway/Liquibase para control de versiones de BD

## Convenciones de código
- Seguir las convenciones estándar de Java (nombres de paquetes en minúsculas, clases en PascalCase, métodos/variables en camelCase).
- Organizar el código por capas: `controller`, `service`, `repository`, `model`/`entity`, `dto`, `exception`.
- Usar DTOs para las respuestas de la API, no exponer entidades JPA directamente.
- Inyección de dependencias por constructor, no por campo (`@Autowired` en campos evitado).
- Manejo de errores centralizado con `@ControllerAdvice` / `@ExceptionHandler`.
- Validación de entrada con `jakarta.validation` (`@Valid`, `@NotNull`, etc.) en los DTOs de entrada.

## Buenas prácticas
- Tests con JUnit 5 y Mockito para lógica de negocio; tests de integración con `@SpringBootTest` cuando aporte valor real.
- No dejar código muerto ni comentarios explicando "qué hace" el código (el código debe ser autoexplicativo); comentarios solo para explicar decisiones no obvias.
- Configuración sensible (credenciales, claves) en variables de entorno o `application-{profile}.yml`, nunca hardcodeada ni commiteada.
- Usar `application.yml` sobre `application.properties` cuando sea posible, por legibilidad en configuraciones anidadas.

## Notas
- Este archivo se irá actualizando a medida que el proyecto avance (arquitectura, base de datos elegida, decisiones importantes).
