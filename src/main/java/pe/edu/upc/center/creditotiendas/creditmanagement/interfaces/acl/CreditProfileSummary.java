package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl;

/**
 * Minimal view of a credit profile for other bounded contexts (Billing). Primitive types only so the Credit
 * model never leaks.
 */
public record CreditProfileSummary(Long id, Long userId, Long storeId, Long currencyId, int cutoffDay, int paymentDay) {
}
