package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.acl.ExternalIamService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;

/**
 * Multi-tenant authorization rules for Store Management, used from controllers as
 * {@code @PreAuthorize("@storeAuthorization.canManageStore(authentication, #storeId)")}.
 *
 * <p>A denied check answers 403. For products of another store the answer is also 403, not 404,
 * so a store admin cannot discover which product ids exist in other stores.</p>
 */
@Component("storeAuthorization")
public class StoreAuthorization {

    private static final String ROLE_STORE_ADMIN = "ROLE_STORE_ADMIN";
    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    private final ExternalIamService externalIamService;
    private final ProductQueryService productQueryService;

    public StoreAuthorization(ExternalIamService externalIamService, ProductQueryService productQueryService) {
        this.externalIamService = externalIamService;
        this.productQueryService = productQueryService;
    }

    /**
     * @return whether the user is a store admin actively linked to the store
     */
    public boolean canManageStore(Authentication authentication, Long storeId) {
        if (!hasRole(authentication, ROLE_STORE_ADMIN) || storeId == null) return false;
        return externalIamService.fetchUserIdByEmail(authentication.getName())
                .map(userId -> externalIamService.isUserAssignedToStore(userId, storeId))
                .orElse(false);
    }

    /**
     * @return whether the user can manage the store that owns the product
     */
    public boolean canManageProduct(Authentication authentication, Long productId) {
        if (productId == null) return false;
        return productQueryService.handle(new GetProductByIdQuery(productId))
                .map(Product::getStoreId)
                .map(storeId -> canManageStore(authentication, storeId))
                .orElse(false);
    }

    /**
     * Store admins of the store see the full catalog; clients see the active catalog.
     * TODO(credit-management): restrict clients to stores where they have an active credit profile.
     */
    public boolean canViewCatalog(Authentication authentication, Long storeId) {
        return canManageStore(authentication, storeId) || isClient(authentication);
    }

    public boolean isClient(Authentication authentication) {
        return hasRole(authentication, ROLE_CLIENT);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
