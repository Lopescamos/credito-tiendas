package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

/**
 * Repayment state of a credit sale. A sale starts ACTIVE; Billing moves it to OVERDUE when an
 * installment is past due and unpaid, and to PAID_ON_TIME or PAID_LATE when it is fully paid.
 */
public enum CreditSaleStatuses {
    ACTIVE,
    OVERDUE,
    PAID_ON_TIME,
    PAID_LATE;

    /** Whether the sale still consumes credit limit. */
    public boolean isOutstanding() {
        return this == ACTIVE || this == OVERDUE;
    }
}
