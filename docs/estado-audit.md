# Estado — Audit

Terminado en la rama `feature/audit` (sin mergear ni pushear todavía, ver `pendientes.md`).

## Tabla (V5)
`operation_logs` (Log_Operacion, US-16). Migración: `V5__create_audit_tables.sql`.

- Sin FK a `users`: el user vive en IAM y no queremos acoplar los schemas; además la bitácora
  tiene que sobrevivir incluso si el usuario se borrara en el futuro.
- `action` como `VARCHAR(20)` + CHECK (persistido con `EnumType.STRING`).
- Índices por `user_id`, `entity` y `occurred_at` para las tres caras de los filtros.
- **Append-only por diseño**: el controller solo expone `GET`; el único escritor es el aspect.
  La API nunca permite editar ni borrar un registro (criterio de aceptación de US-16).

## Modelo
- `OperationLog` (aggregate): `userId` (nullable, p.ej. login con email inexistente),
  `action` (`OperationActions`), `entity` (nombre del aggregate afectado, string),
  `entityId` (nullable), `detail` (hasta 500), `ipAddress` (hasta 45, para IPv6),
  `occurredAt`.
- `OperationActions` (enum): `SIGN_IN`, `SIGN_IN_FAILED`, `CREATE`, `UPDATE`, `DELETE`, `PATCH`,
  `APPROVE`, `REJECT`, `REGISTER`, `ASSIGN`, `UNASSIGN`, `UPLOAD`. Derivados automáticamente del
  prefijo del nombre del command.
- `RegisterOperationCommand` (record): el único command de Audit, lo rellena el aspect.
- `GetOperationLogsQuery` (record): `userId`, `action`, `entity`, `from`, `to`. Cada campo es
  opcional (`null` = sin filtro).

## Aspect (US-16: "vía Spring AOP, no llamadas manuales")
`audit/infrastructure/aop/OperationAuditAspect.java`:
- **Pointcut CRUD:** cualquier
  `pe.edu.upc.center.creditotiendas..application.internal.commandservices.*CommandServiceImpl.handle(..)`,
  excluyendo el paquete `audit` para no crear recursión.
- **Pointcut login:** overload de `UserCommandServiceImpl.handle(SignInCommand)`, registrado
  como `SIGN_IN` si el `Result` es `Success` o `SIGN_IN_FAILED` en caso contrario (los dos
  casos los pide US-16).
- Solo escribe cuando el comando regresó con `Result.Success` (no se audita un fallo; la
  excepción de login sí porque el aspect lo diferencia aparte).
- Usa `@AfterReturning`, no `@Around`: la ejecución del command no cambia si el audit falla.
- Deriva, del nombre de la clase del command:
  - `action` por prefijo (`Create*` → `CREATE`, `Update*StatusCommand` → `PATCH`, etc.).
  - `entity` por strip del prefijo y del sufijo `Command` (p. ej.
    `ApproveCreditPurchaseRequestCommand` → `CreditPurchaseRequest`).
  - `entityId` por reflexión: primer `RecordComponent` tipo `Long` cuyo nombre termina en `Id`
    (normalmente `productId`, `userId`, `creditSaleId`, …). Nulo si no hay ninguno (p. ej.
    creates donde el id aún no existe).
- Lee el `userId` autenticado vía `SecurityContext` + `IamContextFacade.fetchUserIdByEmail`;
  nulo para `anonymousUser` o para login fallido.
- Lee la IP del `HttpServletRequest` actual (`X-Forwarded-For` primero para que Render lo
  respete, luego `remoteAddr`); nulo fuera de un request HTTP (p. ej. jobs de scheduler que
  llaman commands por su cuenta).
- Seed commands y `Simulate*`/`Generate*`/`Mark*` **no se auditan** (no son operaciones
  iniciadas por un usuario real, así el log se mantiene limpio).

## Servicio de persistencia
`OperationLogCommandServiceImpl.handle(RegisterOperationCommand)`:
- `@Transactional(propagation = REQUIRES_NEW)` — el log sobrevive aunque la transacción del
  caller haga rollback más tarde (que no debería pasar porque usamos `@AfterReturning`, pero es
  cinturón y tirantes).
- `try/catch` interno + `log.warn`: un fallo escribiendo el audit **nunca** rompe el command
  que ya se ejecutó bien.

## Endpoints
- `GET /api/v1/operation-logs?userId=&action=&entity=&from=&to=` — solo `SYSTEM_ADMIN`
  (US-16). Fechas en formato ISO-8601 (`2026-11-04T00:00:00Z`). `entity` se compara
  case-insensitively (`product`, `Product`, `PRODUCT`).
- **No hay** `POST/PUT/PATCH/DELETE` en Audit: el único escritor es el aspect.

## Repositorio
Implementación en `OperationLogRepositoryImpl` con `JpaSpecificationExecutor` + `Specification`
dinámica: cada filtro es un `Predicate` opcional. Resultado ordenado por `occurred_at`
descendente (más reciente primero).

## Dependencias nuevas
`spring-boot-starter-aspectj` en `pom.xml` (Spring Boot 4 renombró el antiguo
`spring-boot-starter-aop` a `aspectj`).

## Tests propios
- `OperationLogTest` — construcción del aggregate desde el command, default de `occurredAt`,
  validaciones del record.
- `OperationAuditAspectClassifyTest` — tests unitarios puros (sin Spring) de los tres métodos
  estáticos `actionFromCommand`, `entityFromCommand`, `entityIdFromCommand`: fijan la tabla de
  verbos a prefijos y la extracción del `entityId` por reflexión. Así una renombrada o un verbo
  nuevo falla aquí en vez de registrar silenciosamente un action equivocado.

Un `@WebMvcTest` del controller o un `@DataJpaTest` del adapter quedan como deuda — mismo
estado que Billing. Ver `pendientes.md`.
