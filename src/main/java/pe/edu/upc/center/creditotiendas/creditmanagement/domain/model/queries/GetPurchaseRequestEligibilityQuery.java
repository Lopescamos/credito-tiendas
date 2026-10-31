package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries;

/**
 * Whether the customer may send a remote purchase request (US-08), with every failed rule.
 */
public record GetPurchaseRequestEligibilityQuery(Long creditProfileId) {
}
