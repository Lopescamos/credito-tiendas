package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalIamService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditProfileQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditPurchaseRequestQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditProfileByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;

import java.util.Objects;
import java.util.Optional;

/**
 * Multi-tenant authorization rules for Credit Management, used from controllers as
 * {@code @PreAuthorize("@creditAuthorization.canManageProfile(authentication, #creditProfileId)")}.
 *
 * <ul>
 *   <li><b>Manage</b>: a store admin actively linked to the store of the profile (US-02, US-04, US-12).</li>
 *   <li><b>Own</b>: the CLIENT the profile belongs to (US-03, US-07, US-08, US-09).</li>
 *   <li><b>Access</b>: manage or own.</li>
 * </ul>
 * Anything of another store or another customer answers 403, never 404, so ids are not revealed.
 */
@Component("creditAuthorization")
public class CreditAuthorization {

    private static final String ROLE_STORE_ADMIN = "ROLE_STORE_ADMIN";
    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    private final ExternalIamService externalIamService;
    private final CreditProfileQueryService creditProfileQueryService;
    private final CreditSaleQueryService creditSaleQueryService;
    private final CreditPurchaseRequestQueryService purchaseRequestQueryService;

    public CreditAuthorization(ExternalIamService externalIamService,
                               CreditProfileQueryService creditProfileQueryService,
                               CreditSaleQueryService creditSaleQueryService,
                               CreditPurchaseRequestQueryService purchaseRequestQueryService) {
        this.externalIamService = externalIamService;
        this.creditProfileQueryService = creditProfileQueryService;
        this.creditSaleQueryService = creditSaleQueryService;
        this.purchaseRequestQueryService = purchaseRequestQueryService;
    }

    public boolean canManageStore(Authentication authentication, Long storeId) {
        if (!hasRole(authentication, ROLE_STORE_ADMIN) || storeId == null) return false;
        return currentUserId(authentication)
                .map(userId -> externalIamService.isUserAssignedToStore(userId, storeId))
                .orElse(false);
    }

    public boolean canManageProfile(Authentication authentication, Long creditProfileId) {
        return findProfile(creditProfileId)
                .map(profile -> canManageStore(authentication, profile.getStoreId()))
                .orElse(false);
    }

    public boolean ownsProfile(Authentication authentication, Long creditProfileId) {
        if (!hasRole(authentication, ROLE_CLIENT)) return false;
        var userId = currentUserId(authentication);
        return userId.isPresent() && findProfile(creditProfileId)
                .map(profile -> Objects.equals(profile.getUserId(), userId.get()))
                .orElse(false);
    }

    public boolean canAccessProfile(Authentication authentication, Long creditProfileId) {
        return canManageProfile(authentication, creditProfileId) || ownsProfile(authentication, creditProfileId);
    }

    public boolean canAccessSale(Authentication authentication, Long creditSaleId) {
        return creditSaleId != null && creditSaleQueryService.handle(new GetCreditSaleByIdQuery(creditSaleId))
                .map(CreditSale::getCreditProfileId)
                .map(profileId -> canAccessProfile(authentication, profileId))
                .orElse(false);
    }

    public boolean canManageRequest(Authentication authentication, Long purchaseRequestId) {
        return findRequest(purchaseRequestId)
                .map(request -> canManageProfile(authentication, request.getCreditProfileId()))
                .orElse(false);
    }

    public boolean canAccessRequest(Authentication authentication, Long purchaseRequestId) {
        return findRequest(purchaseRequestId)
                .map(request -> canAccessProfile(authentication, request.getCreditProfileId()))
                .orElse(false);
    }

    private Optional<Long> currentUserId(Authentication authentication) {
        return externalIamService.fetchUserIdByEmail(authentication.getName());
    }

    private Optional<CreditProfile> findProfile(Long creditProfileId) {
        return creditProfileId == null ? Optional.empty()
                : creditProfileQueryService.handle(new GetCreditProfileByIdQuery(creditProfileId));
    }

    private Optional<CreditPurchaseRequest> findRequest(Long purchaseRequestId) {
        return purchaseRequestId == null ? Optional.empty()
                : purchaseRequestQueryService.handle(new GetCreditPurchaseRequestByIdQuery(purchaseRequestId));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
