package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

/**
 * Preview of a credit sale before confirming it (US-02): nothing is saved.
 */
public record CreditSaleSimulationResource(CreditSaleResource sale, PaymentPlanResource paymentPlan) {
}
