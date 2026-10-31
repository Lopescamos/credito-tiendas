package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

/**
 * How a credit sale is repaid (US-02).
 */
public enum SaleModalities {
    /** One payment at the end of a 30-day month (plus grace days, if any). */
    SINGLE_PAYMENT,
    /** Two or more fixed installments every 30 days (French method). */
    INSTALLMENTS
}
