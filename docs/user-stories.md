# User Stories — CreditoTiendasDB

16 user stories, organizadas por actor. Cada una indica el bounded context donde vive su lógica
principal, para que al implementarla sepas en qué capa y en qué `domain/application/infrastructure`
aterriza.

---

## Admin Sistema (web desktop)

### US-13 — Administrar comercios
**BC:** Store Management (`Tienda`)

- **Pre-condición:** usuario autenticado con rol Admin Sistema
- **Flujo:** alta de tienda con RUC, razón social, nombre comercial, dirección, teléfono.
  Activar/desactivar tiendas existentes
- **Criterios de aceptación:**
  - RUC único (11 dígitos), no se permite duplicado
  - Una tienda desactivada no puede recibir nuevas ventas ni nuevos usuarios vinculados
  - El alta no crea automáticamente un Admin Tienda — eso es US-14

### US-14 — Administrar usuarios y roles
**BC:** IAM (`Usuario`, `Rol`, `Usuario_Tienda`)

- **Pre-condición:** usuario autenticado con rol Admin Sistema; para vincular a tienda, la tienda
  debe existir y estar activa (US-13)
- **Flujo:** crear usuario con correo, nombre, apellido, contraseña. Asignar rol (Admin Tienda o
  Cliente). Si el rol es Admin Tienda, vincular a una tienda vía `Usuario_Tienda`
- **Criterios de aceptación:**
  - Correo único en el sistema
  - Contraseña se persiste solo como hash (BCrypt), nunca en texto plano
  - Un usuario con rol Cliente no requiere `Usuario_Tienda` al crearse — su perfil crediticio
    por tienda se crea después, en US-04

### US-16 — Consultar bitácora de operaciones
**BC:** Audit (`Log_Operacion`)

- **Pre-condición:** usuario autenticado con rol Admin Sistema
- **Flujo:** listado filtrable de operaciones del sistema (usuario, acción, módulo afectado,
  rango de fechas), solo lectura
- **Criterios de aceptación:**
  - Toda operación CRUD y todo login queda registrado automáticamente (vía Spring AOP, no por
    llamada manual desde cada servicio)
  - El log incluye: usuario, acción, entidad afectada, id de entidad, dirección IP, timestamp
  - Nunca se puede editar ni borrar un registro de log desde la API

---

## Admin Tienda (web desktop)

### US-01 — Resumen del comercio (dashboard)
**BC:** lectura agregada de Credit Management + Billing

- **Pre-condición:** usuario autenticado con rol Admin Tienda, vinculado a la tienda consultada
- **Flujo:** KPIs — total por cobrar, cobrado este mes, monto en mora; gráfico de cobranza;
  vencimientos próximos; movimientos recientes
- **Criterios de aceptación:**
  - Los datos se filtran siempre por la tienda del usuario autenticado (aislamiento multi-tenant)
  - "Monto en mora" refleja cuotas vencidas impagas a la fecha actual, no histórico

### US-02 — Registrar venta a crédito
**BC:** Credit Management (`Venta_Credito`, `Detalle_Venta`)

- **Pre-condición:** cliente con perfil crediticio activo en la tienda; productos existentes en catálogo
- **Flujo:** wizard — seleccionar cliente → agregar productos del catálogo → elegir modalidad
  (pago único fin de mes o cuotas método francés) → definir plazo y gracia → vista previa del
  plan → confirmar
- **Criterios de aceptación:**
  - Valida que el monto total no exceda el límite de crédito disponible del cliente
  - Al confirmar, copia las 5 columnas de tasa vigente del perfil a `Venta_Credito` (snapshot)
  - Si hay gracia, capitaliza los días entre compra y primer pago antes de generar el cronograma
  - El cronograma de cuotas (`Cuota_Cronograma`) se genera automáticamente al confirmar

### US-04 — Gestionar clientes y condiciones
**BC:** Credit Management (`Perfil_Credito_Cliente`)

- **Pre-condición:** usuario Admin Tienda autenticado; cliente registrado presencialmente la
  primera vez
- **Flujo:** alta/baja/edición de condiciones pactadas — tasa (nominal o efectiva), moneda,
  límite de crédito, plazo máximo, día de corte, día de pago, tasa moratoria
- **Criterios de aceptación:**
  - Editar condiciones de un perfil **no** modifica el snapshot de ventas ya registradas
  - Tasas con mínimo 7 decimales, montos con 2 decimales

### US-05 — Registrar pagos y revisar mora
**BC:** Billing (`Pago_Realizado`, `Imputacion_Pago`, `Cargo_Mora`)

- **Pre-condición:** cliente con cuotas pendientes
- **Flujo:** ver vencimientos pendientes del día, desglose exigible (capital + interés + mora),
  registrar pago íntegro
- **Criterios de aceptación:**
  - No se permite registrar un pago parcial
  - La imputación del pago sigue el orden obligatorio: mora → interés compensatorio → capital
  - La mora se calcula solo sobre la cuota vencida impaga, por días de atraso

### US-06 — Generar estado de cuenta al corte
**BC:** Billing (`Estado_Cuenta_Mensual`, `Detalle_Estado_Cuenta`)

- **Pre-condición:** ha llegado el día de corte configurado en el perfil del cliente
- **Flujo:** snapshot mensual — saldo anterior, compras del período, pagos, intereses, mora,
  saldo final
- **Criterios de aceptación:**
  - Se genera automáticamente en la fecha de corte de cada cliente (no manual)
  - Un estado de cuenta ya generado no se recalcula, aunque cambien condiciones después

### US-11 — Mantener catálogo
**BC:** Store Management (`Producto_Servicio`)

- **Pre-condición:** usuario Admin Tienda autenticado
- **Flujo:** CRUD de productos/servicios — descripción, proveedor, marca, unidad, imagen
  (Cloudinary), precio contado, precio crédito, tipo
- **Criterios de aceptación:**
  - Sin manejo de inventario ni stock
  - Un producto desactivado no aparece en el catálogo que ve el cliente al solicitar (US-08)
    pero sí en ventas históricas

### US-12 — Revisar solicitudes de compra a crédito
**BC:** Credit Management (`Solicitud_Compra_Credito`)

- **Pre-condición:** existe al menos una solicitud en estado Pendiente
- **Flujo:** bandeja de solicitudes recibidas, aprobar (genera venta automáticamente) o rechazar
  con motivo
- **Criterios de aceptación:**
  - Al aprobar, se genera `Venta_Credito` + `Detalle_Venta` automáticamente con las condiciones
    vigentes del perfil, sin pasos manuales adicionales
  - Al rechazar, el motivo es obligatorio y queda visible para el cliente (US-09)

---

## Cliente (web móvil)

### US-03 — Consultar mi cuenta
**BC:** lectura de Credit Management + Billing

- **Pre-condición:** cliente autenticado con perfil activo en al menos una tienda
- **Flujo:** saldo pendiente, próximo pago (monto, fecha, estado), movimientos recientes
- **Criterios de aceptación:**
  - Si el cliente tiene perfil en más de una tienda, cada relación se muestra de forma
    independiente (aislamiento multi-tenant)

### US-07 — Consultar plan de cuotas
**BC:** Billing (`Cuota_Cronograma`)

- **Pre-condición:** cliente con al menos una venta a crédito en cuotas
- **Flujo:** cronograma detallado por cuota — capital, interés, cuota fija, estado; ayuda
  contextual sobre el método francés
- **Criterios de aceptación:**
  - Los valores mostrados son los del snapshot de la venta, no los de las condiciones actuales
    del perfil

### US-08 — Solicitar compra a crédito
**BC:** Credit Management (`Solicitud_Compra_Credito`, `Detalle_Solicitud`)

- **Pre-condición (elegibilidad, todas obligatorias):**
  1. Perfil crediticio activo en la tienda
  2. Al menos una compra previa pagada a tiempo
  3. Sin cuotas vencidas impagas
  4. Monto solicitado dentro del límite de crédito disponible
- **Flujo:** selecciona productos del catálogo de su tienda, envía solicitud
- **Criterios de aceptación:**
  - Si no cumple elegibilidad, la opción de solicitar no está disponible (validar en backend,
    no solo ocultar en frontend)
  - La solicitud queda en estado Pendiente hasta que el Admin Tienda la revise (US-12)

### US-09 — Consultar estado de solicitud
**BC:** Credit Management

- **Pre-condición:** cliente con al menos una solicitud enviada
- **Flujo:** ver si fue aprobada o rechazada, con motivo si aplica; si aprobada, ver la venta
  generada y su cronograma
- **Criterios de aceptación:**
  - El enlace entre solicitud y venta generada es trazable (`id_venta_generada` en
    `Solicitud_Compra_Credito`)

### US-10 — Pagar cuenta
**BC:** Billing

- **Pre-condición:** cliente con monto exigible pendiente
- **Flujo:** ver total exigible con desglose (capital, interés ordinario, mora), confirmar pago íntegro
- **Criterios de aceptación:**
  - Mismas reglas que US-05: sin pagos parciales, prelación mora → interés → capital

### US-15 — Ayuda financiera
**BC:** contenido estático / sin persistencia

- **Pre-condición:** ninguna (accesible a cualquier cliente autenticado)
- **Flujo:** explicaciones de tasa, método francés, gracia total, fecha de corte/pago, mora
- **Criterios de aceptación:**
  - No requiere tablas nuevas; puede servirse como contenido fijo desde el propio BC de
    Credit Management o Billing, o incluso como contenido estático del frontend

---

## Resumen por bounded context

| BC | User Stories |
|---|---|
| IAM | US-14 |
| Store Management | US-11, US-13 |
| Credit Management | US-02, US-04, US-08, US-09, US-12 |
| Billing | US-05, US-06, US-07, US-10 |
| Audit | US-16 |
| Lectura agregada (varios BC) | US-01, US-03 |
| Sin persistencia | US-15 |

Esta tabla es útil al planificar cada rama `feature/<bounded-context>`: muestra qué US debe quedar
cubierta antes de dar esa rama por completa.
