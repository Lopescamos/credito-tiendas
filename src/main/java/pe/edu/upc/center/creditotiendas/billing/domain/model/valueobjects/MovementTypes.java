package pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects;

/**
 * Kind of row in a monthly account statement (model: Detalle_Estado_Cuenta.tipo_movimiento).
 */
public enum MovementTypes {
    PURCHASE,
    PAYMENT,
    INTEREST,
    LATE_FEE
}
