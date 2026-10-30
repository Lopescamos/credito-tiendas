package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl;

/**
 * Public face of the Store Management context for other bounded contexts.
 * Uses primitive types only so the Store Management model never leaks.
 */
public interface StoreManagementContextFacade {

    boolean existsStoreById(Long storeId);

    /**
     * US-13: a deactivated store cannot receive new sales nor newly linked users.
     *
     * @return {@code true} only if the store exists and is active
     */
    boolean isStoreActive(Long storeId);
}
