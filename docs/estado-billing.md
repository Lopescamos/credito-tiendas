# Estado — Billing

Implementado en la rama `feature/billing` (sin commitear todavía — ver `pendientes.md`). Sin
tests propios por ahora. `./mvnw clean verify` pasa con 82 tests (los de Credit Management) y
Flyway aplica `V1..V4` sin problemas en H2.

## Tablas (V4)
`installments` (Cuota_Cronograma), `late_fees` (Cargo_Mora), `payments` (Pago_Realizado) +
`payment_allocations` (Imputacion_Pago), `account_statements` (Estado_Cuenta_Mensual) +
`account_statement_details` (Detalle_Estado_Cuenta). Migración:
`V4__create_billing_tables.sql`.

- `installments.credit_profile_id` está denormalizado (duplica el dato de `credit_sales`) para
  evitar joins entre bounded contexts.
- FKs a `credit_sales` y `credit_profiles` (de Credit Management).

## Modelo
- `Installment` (aggregate): número de cuota, fecha de vencimiento, capital, interés, cuota
  fija, saldo restante, estado (`InstallmentStatuses`: PENDING/OVERDUE/PAID_ON_TIME/PAID_LATE),
  fecha de pago.
- `LateFee` (aggregate): mora calculada por cuota (días de atraso + monto), ligada 1 a 1 por
  recálculo (reemplaza, no suma) a una `Installment`.
- `Payment` (aggregate) + `PaymentAllocation` (entidad hija): un pago cubre una o más cuotas
  **de una sola venta**; cada imputación registra cuánto fue a mora, interés y capital,
  siguiendo la prelación de `PaymentAllocationPolicy` de Credit Management.
- `AccountStatement` (aggregate) + `AccountStatementDetail` (entidad hija): estado de cuenta
  mensual por perfil (`period` tipo `YYYY-MM`), con saldo anterior, compras, pagos, interés y
  mora del periodo, y saldo final.

## Reutilización de la lógica pura de Credit Management
`PaymentPlanCalculator`, `LateFeeCalculator` y `PaymentAllocationPolicy` (prototipos puros en
`creditmanagement/domain/services`) se reutilizan desde Billing — no se reimplementa la
fórmula financiera.

## Piezas clave

- **`CreditSaleRegisteredEventHandler`** — escucha `CreditSaleRegisteredIntegrationEvent`
  (emitido por Credit Management al registrar una venta) y crea las cuotas vía
  `CreateInstallmentScheduleCommand`, a partir del `ScheduledInstallment` calculado. Es
  **idempotente**: si las cuotas de esa venta ya existen, no las duplica. El listener es
  **síncrono** (`@EventListener`): si Billing falla al crear las cuotas, la venta se revierte
  y el usuario ve el error de inmediato — no queda una venta sin cronograma.
- **`BillingScheduler`** (`billing/infrastructure/scheduling/`) — dos jobs `@Scheduled` en hora
  de Lima (`America/Lima`):
  - `0 5 0 * * *` — barrido de vencidas: marca `OVERDUE` toda cuota vencida y actualiza la
    venta dueña en Credit.
  - `0 10 0 * * *` — genera el estado de cuenta de cada perfil activo cuyo día de corte
    (`cutoffDay`) es hoy, si no existe ya uno para ese periodo.
  - Ambos corren 5-10 minutos después de medianoche para que un pago registrado el mismo día de
    vencimiento nunca quede marcado como mora por error.
- **`ExternalCreditService`** — ACL hacia Credit Management: `fetchProfile`,
  `fetchActiveProfileIdsByCutoffDay`, `markCreditSaleAsOverdue/PaidOnTime/PaidLate`.

## Endpoints

| Ruta | Quién | Historia |
|---|---|---|
| `GET /api/v1/credit-sales/{id}/installments` | dueño o Admin Tienda | US-07 (plan real con estado) |
| `GET /api/v1/stores/{id}/installments-due` | Admin Tienda de la tienda | US-05 (dashboard de vencimientos) |
| `GET /api/v1/credit-profiles/{id}/payment-due?installmentIds=…` | dueño o Admin Tienda | US-05 / US-10 (desglose con mora al día) |
| `POST /api/v1/credit-profiles/{id}/payments` | dueño o Admin Tienda | US-05 / US-10 (registrar pago) |
| `GET /api/v1/credit-profiles/{id}/payments` | dueño o Admin Tienda | histórico |
| `GET /api/v1/credit-profiles/{id}/account-statements` | dueño o Admin Tienda | US-06 (lista) |
| `GET /api/v1/account-statements/{id}` | autenticado | US-06 (detalle) |

## Decisiones

1. **Un pago cubre cuotas de una sola venta.** El evento de pago, la mora y la prelación tienen
   más sentido así. Si el cliente debe pagar cuotas de varias ventas, es un pago por venta.
2. **El cliente no manda el monto del pago.** El servidor lo calcula desde las cuotas elegidas
   a la fecha de pago, así no puede subpagar por accidente (y `PaymentAllocationPolicy` ya
   exige exactitud).
3. **`creditProfileId` denormalizado en `Installment`.** Evita que Billing haga JOIN a tablas
   de Credit en consultas rutinarias (`findAllUnpaidByProfileId`, scheduler).
4. **Evento sincrónico (`@EventListener`)** — ver "Piezas clave" arriba.
5. **Scheduler en zona Lima**, 00:05 (barrido de vencidas) y 00:10 (estados de cuenta).
6. **`AccountStatement`**: `finalBalance = previousBalance + periodPurchases + periodInterest +
   periodLateFees − periodPayments`. Las "purchases" se aproximan como el capital de las
   cuotas que vencen en el período (Billing no tiene la venta directamente), lo que concuerda
   con el flujo contable del cronograma. Ver pendiente sobre esta aproximación en
   `pendientes.md`.

## ACL consumida
`ExternalCreditService` (`billing/application/internal/outboundservices/acl/`) envuelve
`CreditManagementContextFacade`.

## ACL expuesta a Credit Management
`billing/interfaces/acl/BillingContextFacade.java` (+
`billing/application/acl/BillingContextFacadeImpl.java`): `fetchPendingCapitalByCreditProfileId`
suma el `capital` de toda cuota `PENDING`/`OVERDUE` del perfil. La consume Credit como
`ExternalBillingService` para calcular el crédito disponible v2 (límite − capital pendiente −
solicitudes pendientes) — ver "Integración con Billing" en `docs/estado-credit.md`. Es la
primera vez que existe una dependencia Credit → Billing (antes solo Billing → Credit),
aceptada como el mismo patrón de ciclo de ACL que IAM ↔ Store.

## Comprobado con curl

- Venta nueva → evento → 3 cuotas `PENDING` persistidas (`GET /credit-sales/1/installments`).
- `payment-due` muestra desglose (mora, interés, capital, total).
- Pago en fecha (cuota con vencimiento 2026-11-30): 201, `allocation.lateFeeAmount = 0`, cuota
  → `PAID_ON_TIME`.
- Doble pago sobre la misma cuota: 422 `BUSINESS_RULE_VIOLATION`.
- Pago con 10 días de atraso: `totalAmount = 350.23`, `allocation.lateFeeAmount = 3.48` —
  exactamente el valor calculado con Python en los tests unitarios de Credit Management (TNA
  36% diaria, base 346.75 → 3.48).
- Venta → `PAID_LATE` cuando se paga la última cuota si alguna fue `PAID_LATE`.
- Multi-tenant: cliente y Admin Tienda intentando pagar en un perfil ajeno → 403.
- Mora acumulada: forzando 1034 días de atraso, la mora da 116.58 por cuota.
- Build: `./mvnw clean verify` pasa con 82 tests y Flyway aplica `V1..V4`.

## Pendiente de este BC
Ver `pendientes.md` para el detalle completo (account statements no probados end-to-end,
fórmula de "purchases" aproximada, Credit no escucha sus propios eventos, scheduler no probado
con cron real, sin tests propios, V1-V4 sin probar contra PostgreSQL real).
