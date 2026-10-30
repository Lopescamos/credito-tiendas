package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries;

/**
 * Every catalog item of a store, active or not (store admin view).
 */
public record GetAllProductsByStoreIdQuery(Long storeId) {
}
