package pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects;

/**
 * Repayment state of one installment (model: Cuota_Cronograma).
 *
 * <p>A fresh installment is {@code PENDING}. Billing's nightly job turns it into {@code OVERDUE} when the
 * due date passes with no payment. A paid installment ends in {@code PAID_ON_TIME} or {@code PAID_LATE}.</p>
 */
public enum InstallmentStatuses {
    PENDING,
    OVERDUE,
    PAID_ON_TIME,
    PAID_LATE;

    public boolean isPaid() {
        return this == PAID_ON_TIME || this == PAID_LATE;
    }

    public boolean isDue() {
        return this == PENDING || this == OVERDUE;
    }
}
