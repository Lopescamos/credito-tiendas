package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl;

import java.math.BigDecimal;

/**
 * Public face of the Store Management context for other bounded contexts.
 * Uses primitive and JDK types only so the Store Management model never leaks.
 */
public interface StoreManagementContextFacade {

    boolean existsStoreById(Long storeId);

    /**
     * US-13: a deactivated store cannot receive new sales nor newly linked users.
     *
     * @return {@code true} only if the store exists and is active
     */
    boolean isStoreActive(Long storeId);

    boolean existsCurrencyById(Long currencyId);

    /**
     * Credit price of a catalog item that can be sold right now in the given store.
     *
     * @return the credit price, or {@link BigDecimal#ZERO} if the product does not exist, belongs to
     *         another store or is deactivated
     */
    BigDecimal fetchAvailableProductCreditPrice(Long productId, Long storeId);
}
