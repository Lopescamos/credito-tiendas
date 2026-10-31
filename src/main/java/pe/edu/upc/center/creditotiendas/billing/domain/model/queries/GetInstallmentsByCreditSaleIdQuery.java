package pe.edu.upc.center.creditotiendas.billing.domain.model.queries;

/**
 * Real installments with their current state (US-07). Replaces the plan from Credit once Billing exists.
 */
public record GetInstallmentsByCreditSaleIdQuery(Long creditSaleId) {
}
