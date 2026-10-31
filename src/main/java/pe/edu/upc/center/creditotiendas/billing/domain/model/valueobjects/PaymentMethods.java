package pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects;

/**
 * How a payment was received by the store (model: Pago_Realizado.metodo_pago).
 */
public enum PaymentMethods {
    CASH,
    BANK_TRANSFER,
    YAPE,
    PLIN,
    DEBIT_CARD,
    CREDIT_CARD
}
