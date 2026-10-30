package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries;

/**
 * Only the active catalog items of a store (customer view, US-08).
 */
public record GetActiveProductsByStoreIdQuery(Long storeId) {
}
