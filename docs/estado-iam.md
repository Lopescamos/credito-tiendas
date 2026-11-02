# Estado — IAM

Terminado. Commiteado en `feature/iam`, mergeado y publicado en `develop`. Sin tests propios.

## Tablas
`roles` (Rol), `users` (Usuario), `user_stores` (Usuario_Tienda). Clases en inglés; la naming
strategy genera los nombres de tabla. Migración: `V1__create_iam_tables.sql`.

## Modelo
- `User` (aggregate) con un solo rol (`Roles`: `SYSTEM_ADMIN`, `STORE_ADMIN`, `CLIENT`) y sus
  `UserStore` como entidades hijas. Desasignar una tienda desactiva el vínculo (no lo borra);
  reasignar reactiva la misma fila.
- `Role` es catálogo sembrado, sin API de escritura.

## Endpoints
- `POST /api/v1/authentication/sign-in` — público, devuelve JWT + datos del usuario + `storeIds`
- `POST /api/v1/users` — `SYSTEM_ADMIN` (cualquier rol asignable) o `STORE_ADMIN` (solo rol
  `CLIENT`, registro presencial del cliente, US-04). Ver `IamAuthorization.canCreateUserWithRole`.
- `GET|PUT /api/v1/users/{id}`, `PATCH /api/v1/users/{id}/status`,
  `POST /api/v1/users/{id}/stores`, `DELETE /api/v1/users/{id}/stores/{storeId}`,
  `GET /api/v1/users` — solo `SYSTEM_ADMIN`
- `GET /api/v1/users/me` — cualquier usuario autenticado
- `GET /api/v1/roles` — solo `SYSTEM_ADMIN`

## Reglas de negocio
Email único (normalizado a minúsculas); por API solo se crean `STORE_ADMIN`/`CLIENT`; el
`SYSTEM_ADMIN` no se puede desactivar; solo un `STORE_ADMIN` activo se vincula a tiendas.

Quién crea a quién (US-14 vs. US-04): `SYSTEM_ADMIN` da de alta tiendas y Admin Tienda; el
**Admin Tienda registra a sus propios clientes** en persona, creándolos con rol `CLIENT` vía
`POST /api/v1/users` y, acto seguido, abriéndoles su `Perfil_Credito_Cliente` en su tienda
(`POST /api/v1/stores/{storeId}/credit-profiles`, ya restringido a `STORE_ADMIN` de esa tienda —
ver `docs/estado-credit.md`). Un `STORE_ADMIN` nunca puede crear otro `STORE_ADMIN` ni un
`SYSTEM_ADMIN` por esta vía: `IamAuthorization.canCreateUserWithRole` lo bloquea en el
`@PreAuthorize` del controller, antes de que la petición llegue al command service.

## Seeding
`iamApplicationReadyEventHandler`: 3 roles + Admin Sistema inicial desde `ADMIN_EMAIL`/
`ADMIN_PASSWORD` (si faltan, se omite con warning; si son inválidos, la app no arranca).

## Seguridad
- JWT HMAC con `iss` verificado, `sub` = email, claims `userId` y `role`.
- La app no arranca si `JWT_SECRET` tiene menos de 32 bytes.
- El filtro recarga el usuario desde BD en cada request: desactivar o cambiar el rol aplica al
  instante.
- BCrypt (contraseña de 8–72 bytes).
- El login responde igual y tarda igual con email inexistente o clave errónea.
- Stateless, sin CSRF.
- CORS por `cors.allowed-origins`.
- 401/403 en formato `ErrorResource`.
- H2 console solo en dev.

## ACL para otros BCs
`IamContextFacade` → `fetchUserIdByEmail`, `fetchUserRoleByUserId`, `isUserAssignedToStore`
(para el aislamiento multi-tenant).

## Decisiones tomadas
- `HashingService`/`TokenService` van en `application/internal/outboundservices`, no en
  `domain/services` como indica la tabla 4.4 de la guía.
- Enums persistidos con `EnumType.STRING`.
- `user_stores.store_id` sin FK JPA, porque es otro BC.
- Validaciones de formato en el constructor compacto de los commands (→ 400).

## Verificación
A mano con curl (login, 401/403, 409, 422, desactivación inmediata, CORS); `contextLoads` en
verde. Al vincular un usuario a una tienda, IAM valida vía `ExternalStoreService` (ACL de Store)
que la tienda exista (404) y esté activa (422).
