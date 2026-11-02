# Estado — Credit Management (core domain)

Terminado. Commiteado en `feature/credit-management` y mergeado en `develop` local. 82 tests en
verde con `./mvnw clean verify`.

## Tablas (V3)
`credit_profiles` (Perfil_Credito_Cliente), `credit_sales` (Venta_Credito, con las 5 columnas
`*_snapshot`), `sale_details` (Detalle_Venta), `credit_purchase_requests`
(Solicitud_Compra_Credito), `purchase_request_details` (Detalle_Solicitud). Migración:
`V3__create_credit_management_tables.sql`.

- FKs a users, stores, currencies y products.
- FK circular solicitud ↔ venta generada, agregada con `ALTER TABLE` (trazabilidad US-09).
- CHECKs de estados, modalidad/cuotas y respuesta de la solicitud.

## Lógica financiera pura
En `domain/services` y `valueobjects`, probada con tests unitarios (valores esperados
calculados aparte con Python `Decimal`):

- `InterestRate` (TEA, o TNA + `CapitalizationPeriods`) y `LateInterestRate` → TED y tasa de
  `d` días, en base 360.
- `PaymentPlanCalculator`:
  - Método francés con gracia total: `C' = C₀(1+TED)^dg`, `R = C'·i/(1−(1+i)^−n)`; con `i = 0`,
    `R = C'/n`.
  - Interés por cuota = saldo × i; la última cuota absorbe el redondeo.
  - La cuota `k` vence a `dg + 30k` días.
  - Los factores derivados de `double` se redondean a 12 decimales (evita ruido tipo
    `0.020000000000000684`).
- `LateFeeCalculator`:
  - Mora = `saldoVencido × [(1+TEDmora)^díasAtraso − 1]`, solo sobre la cuota vencida.
  - Pagar en la fecha de vencimiento no genera mora.
  - Se recalcula desde el vencimiento (reemplaza, no suma); calcularla en dos tramos da lo
    mismo que de una vez.
- `PaymentAllocationPolicy`:
  - Prelación: primero toda la mora, luego todo el interés y luego todo el capital; la cuota
    más antigua primero.
  - Sin pagos parciales: el pago debe ser exacto (rechaza un céntimo de menos o de más).
- Mora y prelación son prototipos puros: Billing los usa para `Cargo_Mora` e `Imputacion_Pago`.
- `PurchaseRequestEligibilityPolicy`: las 4 reglas de US-08; lista todas las que fallan.

## Modelo
- `CreditProfile`: un perfil por cliente y tienda (409).
  - `saleRejectionReason`: perfil activo, monto ≤ crédito disponible, plazo ≤ máximo.
  - `availableCredit(pendingCapital, reservedCredit)` = límite − capital pendiente (de Billing)
    − reservado por solicitudes pendientes, nunca negativo (v2, ver decisión 5 abajo). El
    cálculo ya no vive en el aggregate solo con las ventas: recibe los dos montos ya sumados
    por la capa de aplicación.
- `CreditSale.register` copia las tasas del perfil (snapshot) y calcula `C'` y `R`.
  - `paymentPlan()` se recalcula siempre desde el snapshot, nunca desde el perfil.
- `CreditPurchaseRequest`: PENDING → APPROVED (con `generatedSaleId`) o REJECTED (con motivo
  obligatorio). Solo se responde estando pendiente (409).

## Decisiones confirmadas por el grupo
1. La mora nominal se capitaliza siempre a diario: es la constante
   `LateInterestRate.CAPITALIZATION_PERIOD_DAYS = 1`, sin columna propia.
2. Las cuotas vencen cada 30 días desde el fin de la gracia; el día de pago del perfil es solo
   informativo.
3. "Pago único fin de mes" = método francés con n = 1 a 30 días.
4. Una solicitud aprobada siempre es pago único sin gracia, con los precios de la solicitud y
   las tasas vigentes del perfil al aprobar. El cliente nunca pide en cuotas desde la app
   (alcance intencional).
5. Crédito disponible v2 = límite − capital pendiente de cuotas no pagadas (Billing) − monto de
   las solicitudes de compra remota aún pendientes (US-08) del mismo perfil. Reemplaza a v1
   (límite − total de ventas no pagadas): v1 subestimaba al contar el total de una venta aunque
   ya se hubieran pagado cuotas, y no reservaba crédito para solicitudes aún no respondidas.
6. `sale_date` es DATE con la fecha de Lima (`CreditSale.BUSINESS_ZONE`); la hora exacta queda
   en `created_at`.
- Las tasas se guardan como porcentaje anual `DECIMAL(10,7)` (45.0000000 = 45 %).

## Estados de venta
`ACTIVE`, `OVERDUE`, `PAID_ON_TIME`, `PAID_LATE`. Los cambia Billing. La elegibilidad lee estos
estados localmente, así no hace falta consultar a Billing (evita el ciclo Credit ↔ Billing).

## Endpoints
- `POST|GET /api/v1/stores/{storeId}/credit-profiles` — `STORE_ADMIN` de la tienda (US-04)
- `GET /api/v1/credit-profiles/me` — `CLIENT` (US-03)
- `GET|PUT /api/v1/credit-profiles/{id}`, `PATCH .../status` — leer: dueño o su `STORE_ADMIN`;
  editar: `STORE_ADMIN`
- `POST .../{id}/credit-sale-simulations` (vista previa sin guardar) y
  `POST|GET .../{id}/credit-sales` (US-02)
- `GET /api/v1/credit-sales/{id}` y `.../payment-plan` (US-07, siempre desde el snapshot)
- `GET .../{id}/purchase-request-eligibility`, `POST|GET .../{id}/purchase-requests` (crear:
  solo el cliente dueño, 422 si no es elegible) (US-08, US-09)
- `GET /api/v1/stores/{storeId}/purchase-requests?status=` y
  `POST /api/v1/purchase-requests/{id}/approval|rejection` (US-12). Aprobar crea la venta y
  responde la solicitud en una sola transacción.

## Aislamiento multi-tenant
El bean `creditAuthorization` (`canManageStore`, `canManageProfile`, `ownsProfile`,
`canAccessProfile/Sale/Request`) responde 403, nunca 404, ante recursos de otra tienda o de otro
cliente.

## ACL consumidas
`creditManagementExternalStoreService` y `creditManagementExternalIamService`. Llevan nombre de
bean explícito porque IAM y Store tienen clases homónimas. `ExternalBillingService`
(sin colisión de nombre) consume `BillingContextFacade` de Billing para el capital pendiente
usado en crédito disponible v2 — ver "Integración con Billing" abajo.

## Integración con Billing (crédito disponible v2 y reserva de solicitudes)
- `billing/interfaces/acl/BillingContextFacade.java` (+ `billing/application/acl/BillingContextFacadeImpl.java`)
  expone `fetchPendingCapitalByCreditProfileId`: suma el `capital` de toda cuota `PENDING`/`OVERDUE`
  del perfil (vía `InstallmentRepository.findAllUnpaidByCreditProfileId`, ya existente en Billing).
- Credit la consume como `ExternalBillingService` en
  `creditmanagement/application/internal/outboundservices/acl/`. Esto crea una dependencia
  Credit → Billing (antes solo existía Billing → Credit), aceptada a propósito como el mismo
  tipo de ciclo de ACL que ya hay entre IAM ↔ Store: no hay ciclo de beans de Spring porque
  ninguna de las dos fachadas depende de la otra.
- `CreditSaleCommandServiceImpl`, `CreditProfileQueryServiceImpl` y
  `CreditPurchaseRequestCommandServiceImpl` calculan `reservedCredit` sumando el `totalAmount`
  de las solicitudes `PENDING` del mismo perfil (excluyendo la propia solicitud cuando se
  re-chequea su aprobación), y pasan `pendingCapital` + `reservedCredit` a
  `CreditProfile.availableCredit(...)`.

## ACL expuestas a Billing
`creditmanagement/interfaces/acl/CreditManagementContextFacade.java` (+ `CreditProfileSummary`)
y la implementación `creditmanagement/application/acl/CreditManagementContextFacadeImpl.java`.
Billing la consume como `ExternalCreditService` en
`billing/application/internal/outboundservices/acl/`.

## Integration event emitido
`creditmanagement/interfaces/events/CreditSaleRegisteredIntegrationEvent.java` — se publica al
registrar una venta; Billing lo escucha para generar el cronograma de cuotas (ver
`estado-billing.md`).

## Verificación
Con curl punta a punta: plan 346.75/346.75/346.77; editar el perfil no cambia la venta. Límite
y plazo → 422; acceso de otra tienda → 403. Elegibilidad 422 → tras marcar una venta
`PAID_ON_TIME` (simulando Billing) → solicitud, aprobación (venta enlazada) y rechazo con
motivo.
