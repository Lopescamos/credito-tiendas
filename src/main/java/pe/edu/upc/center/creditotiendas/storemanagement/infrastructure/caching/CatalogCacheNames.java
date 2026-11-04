package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.caching;

/**
 * Names of the caches used by Store Management, shared between the query service that reads
 * through the cache ({@code @Cacheable}) and the command service that invalidates it on writes.
 */
public final class CatalogCacheNames {

    private CatalogCacheNames() {
    }

    /** Active products of a store (US-08 catalog), keyed by {@code storeId}. */
    public static final String ACTIVE_PRODUCTS_BY_STORE = "active-products-by-store";
}
