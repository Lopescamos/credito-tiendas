package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

/**
 * Reasons why a customer cannot send a remote credit purchase request (US-08).
 */
public enum EligibilityFailures {
    PROFILE_INACTIVE,
    NO_SALE_PAID_ON_TIME,
    HAS_OVERDUE_INSTALLMENTS,
    AMOUNT_EXCEEDS_AVAILABLE_CREDIT
}
