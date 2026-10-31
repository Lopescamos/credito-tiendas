package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries;

/**
 * Credit limit minus the amount of the sales not fully paid yet.
 */
public record GetAvailableCreditByCreditProfileIdQuery(Long creditProfileId) {
}
