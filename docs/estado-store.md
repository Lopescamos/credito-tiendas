# Estado — Store Management

Terminado. Commiteado en `feature/store-management`, mergeado y publicado en `develop`. Sin
tests propios.

## Tablas (V2)
`stores` (Tienda), `currencies` (Moneda), `measurement_units` (Unidad_Medida), `products`
(Producto_Servicio). Migración: `V2__create_store_management_tables.sql`.

## Modelo
- `Store`: RUC (VO `Ruc`, 11 dígitos, único, no editable), razón social, nombre comercial,
  dirección, teléfono y si está activa.
- `Product`: `storeId`, nombre, descripción, tipo (`ProductTypes` PRODUCT/SERVICE), proveedor,
  marca, `measurementUnitId` opcional, `imageUrl`, precio contado y crédito (`BigDecimal`, 2
  decimales) y si está activo. Sin stock; no se borra, se desactiva.
- `Currency` y `MeasurementUnit`: catálogos sin API de escritura.
- Los aggregates se referencian por id (sin relaciones JPA); las FKs reales están en V2.

## Endpoints
- `POST|GET /api/v1/stores`, `PUT /api/v1/stores/{id}`, `PATCH /api/v1/stores/{id}/status` —
  solo `SYSTEM_ADMIN` (US-13)
- `GET /api/v1/stores/{id}` — cualquier usuario autenticado
- `POST /api/v1/stores/{storeId}/products` — `STORE_ADMIN` vinculado a esa tienda
- `GET /api/v1/stores/{storeId}/products` — su `STORE_ADMIN` ve todo; `CLIENT` solo activos
  (US-08)
- `GET|PUT /api/v1/products/{id}`, `PATCH /api/v1/products/{id}/status`,
  `POST /api/v1/products/{id}/image` (multipart `file`) — `STORE_ADMIN` de la tienda dueña
- `GET /api/v1/currencies`, `GET /api/v1/measurement-units` — cualquier usuario autenticado

## Reglas de negocio
- RUC único (409).
- Una tienda desactivada no recibe productos nuevos ni usuarios vinculados (422).
- La unidad indicada debe existir (404).
- Precios > 0 con máximo 2 decimales y 10 enteros.
- Imagen JPEG/PNG/WebP de hasta 5 MB (más grande → 413).

## Aislamiento multi-tenant
El bean `storeAuthorization` (`storemanagement/infrastructure/security/StoreAuthorization`) se
usa en `@PreAuthorize` (`canManageStore`, `canManageProduct`, `canViewCatalog`) y pregunta a IAM
vía `ExternalIamService`. Un producto de otra tienda responde 403 (no 404) para no revelar ids
ajenos.

## Cloudinary
- El puerto `FileStorageService` (+ `FileStorageException`) está en
  `application/internal/outboundservices` y la implementación plana en
  `infrastructure/filestorage/` (`CloudinarySettings`, `CloudinaryStorageService`), según la
  sección 4.3 de la guía.
- Cada producto tiene un nombre fijo (`creditotiendas/products/product-{id}`), así que una
  imagen nueva reemplaza a la anterior.
- Con `CLOUDINARY_URL` vacío la app arranca igual y solo falla la subida.

## Seeding
`storeManagementApplicationReadyEventHandler`: PEN/USD y 10 unidades; inserta solo los
faltantes.

## ACL para otros BCs
`StoreManagementContextFacade` → `existsStoreById`, `isStoreActive`, `existsCurrencyById`,
`fetchAvailableProductCreditPrice` (esta última para Credit).

## Verificación
A mano con curl (RUC duplicado/inválido, tienda inexistente/desactivada al vincular, 403 entre
tiendas, catálogo de cliente solo activos, imagen 400/413); `contextLoads` en verde con V1 + V2.
