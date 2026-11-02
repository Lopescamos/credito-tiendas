# Pendientes y decisiones abiertas

## Pendientes activos

- **Push de `feature/credit-available-v2`, `bugfix/test-profile-dotenv-leak` y `develop`.**
  Ambas ramas commiteadas y mergeadas (`--no-ff`) a `develop` local; falta subir las tres a
  GitHub.
- **Billing sin tests propios.** Los cálculos los hace la lógica financiera ya probada en
  Credit; para `PaymentCommandServiceImpl` convendría un `@DataJpaTest` o `@SpringBootTest`
  cuando toque. Tampoco hay `@WebMvcTest` de sus controllers.
- **Account statements sin probar end-to-end.** No se disparó el scheduler desde curl (no hay
  endpoint para forzarlo). Al correr automático en el `cutoffDay` genera lo esperado; para
  pruebas ayudaría un endpoint admin que lance los jobs manualmente (útil también para la
  demo).
- **Scheduler de Billing no probado end-to-end.** Los cron están configurados y la lógica
  subyacente se probó con un `UPDATE` directo en la BD, no disparando el `@Scheduled` real.
- **Fórmula de "purchases del período" en `AccountStatement` es aproximada** (suma de capital
  de cuotas que vencen en el rango, no el monto real de las ventas registradas en el período).
  Si se quiere el monto real, hay que exponer eso en la fachada de Credit
  (`CreditManagementContextFacade`).
- **Credit no escucha sus propios eventos.** `CreditSaleRegisteredEventHandler` vive en
  Billing, no en Credit Management. Si Credit necesita reaccionar a sus propios eventos en el
  futuro, hay que agregarle su propio `@EventListener`.
- **Migraciones V1–V4 nunca probadas con Flyway contra PostgreSQL real** (ver el pendiente
  general de abajo — Billing agrega V4 a esa misma brecha).
- **Catálogo visible para cualquier cliente.** Hoy un `CLIENT` ve el catálogo activo de
  cualquier tienda. Debería limitarse a las tiendas donde tiene perfil activo
  (`TODO(credit-management)` en `StoreAuthorization.canViewCatalog`). Resolverlo desde Store
  crearía el ciclo Store ↔ Credit; conviene servir el catálogo del cliente desde Credit.
- **US-15 (ayuda financiera):** contenido fijo en el frontend, como permite la US.
- **Subida real a Cloudinary sin probar con imagen válida.** Solo se verificó que Cloudinary
  rechaza un archivo falso (`Invalid image file`). No se subió una imagen real para no publicar
  archivos en la cuenta sin permiso.
- **Migraciones V1–V4 no probadas contra PostgreSQL real.** Solo se validaron en H2 (no hay
  Postgres local y el daemon de Docker estaba apagado). Probar antes del primer deploy: levantar
  Postgres en Docker o apuntar a Supabase con perfil `prod`.
- **Reglas opcionales no aplicadas** (no las pide el enunciado):
  - dígito verificador del RUC (SUNAT);
  - precio crédito ≥ precio contado.
- **Dependencia mutua IAM ↔ Store** vía ACL. IAM pregunta si la tienda está activa; Store
  pregunta si el usuario está vinculado. No hay ciclo de beans y la app arranca, pero hay que
  tenerlo presente.
- **Tests de IAM y de Store Management** no se escribieron (decisión del usuario). Credit sí
  tiene tests de su lógica financiera, pero no de integración ni de API.
- **`@DataJpaTest` necesita `@Import(JpaAuditingConfiguration.class)`.** `@EnableJpaAuditing`
  vive en `shared/infrastructure/persistence/jpa/configuration/JpaAuditingConfiguration`, no en
  `Main` (en `Main` rompe los `@WebMvcTest`). Los test slices no escanean `@Configuration`, así
  que sin el `@Import` `createdAt`/`updatedAt` quedan `null` y el insert falla por
  `nullable = false`. Esto contradice el checklist de la guía ("Main lleva
  `@EnableJpaAuditing`") a propósito.
- **Variables nuevas:**
  - En el `.env` local: `ADMIN_EMAIL` y `ADMIN_PASSWORD` (sin ellas, en dev nadie puede iniciar
    sesión).
  - En Render: `CORS_ALLOWED_ORIGINS` (obligatoria en prod, sin default; si falta, la app no
    arranca), `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
- **Sin límite de intentos en `/sign-in`** (fuerza bruta). No lo pide el enunciado; mejora
  opcional.

## Decisiones abiertas

Ninguna por ahora.

## Resueltas

- Idioma: clases en inglés, tablas generadas por la naming strategy.
- Esquema en prod: Flyway (ver `docs/estado-credit.md` → Flyway, y las migraciones V1-V4).
- Seguridad configurada en IAM (ya no aparece la contraseña autogenerada de Spring).
- `references/` ya no existe en el repo.
- Validar tienda al vincular usuario (IAM ↔ Store) y FK `user_stores.store_id → stores.id`
  (en V2).
- Las 6 decisiones de Credit Management (ver `docs/estado-credit.md`).
- Rutas de docs corregidas (`docs/guia-arquitectura-por-capas-ddd.md`, `docs/modelo-bd.puml`).
- **"Pago íntegro" (US-05/US-10):** un pago cubre cuotas de una sola venta; el cliente no manda
  el monto, el servidor lo calcula desde las cuotas elegidas a la fecha de pago (ver las 6
  decisiones de Billing en `docs/estado-billing.md`).
- **Billing commiteado y publicado.** 3 commits agrupados por capa en `feature/billing`
  (identidad propia del usuario, sin coautoría de Claude), `merge --no-ff` a `develop`,
  y los docs de la reestructuración (`chore/restructure-context`) también mergeados a
  `develop`. Las tres ramas (`feature/billing`, `chore/restructure-context`, `develop`) están
  al día en GitHub.
- **Crédito disponible v2.** `CreditProfile.availableCredit` ahora recibe `pendingCapital`
  (de Billing, vía la nueva `BillingContextFacade`/`ExternalBillingService`) y `reservedCredit`
  (solicitudes `PENDING` del mismo perfil), en vez de la lista de ventas. Afecta venta directa
  (US-02), creación y aprobación de solicitud remota (US-08/US-12) y las dos consultas de
  `CreditProfileQueryServiceImpl`. Detalle en `docs/estado-credit.md` → "Integración con
  Billing".
- **Las solicitudes pendientes ahora reservan crédito.** Resuelto como parte del punto
  anterior: `reservedCredit` suma el `totalAmount` de las solicitudes `PENDING` del perfil
  (excluyendo la propia solicitud al re-chequear su aprobación), así que varias solicitudes
  pendientes ya no pueden juntas superar el límite.
- **El perfil `test` ya no usa el `.env`.** `springdotenv.enabled=false` en
  `application-test.properties` — verificado con el `.env` real del equipo (que sí tiene
  `CLOUDINARY_URL`) sin que se filtre a los tests.
- **Quién registra clientes.** El Admin Tienda registra a sus propios clientes en persona:
  `POST /api/v1/users` ahora acepta `STORE_ADMIN` cuando el rol creado es `CLIENT` (nunca
  `STORE_ADMIN` ni `SYSTEM_ADMIN`), vía el nuevo `IamAuthorization.canCreateUserWithRole`. El
  resto del flujo (abrir el perfil de crédito, registrar la primera venta, elegibilidad para
  solicitudes remotas) ya funcionaba así — ver `docs/estado-iam.md`.

## Siguiente (después de Billing)

- **Audit** (transversal, último BC): `Log_Operacion`, Spring AOP (US-16).
