package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

/**
 * Parts of an amount owed, declared in the mandatory order a payment covers them:
 * late fee, then compensatory interest, then capital.
 */
public enum PaymentComponents {
    LATE_FEE,
    INTEREST,
    CAPITAL
}
