package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditPurchaseRequestQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PurchaseRequestStatuses;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditPurchaseRequestResourceFromEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Inbox of purchase requests received by a store (US-12).
 */
@RestController
@RequestMapping(value = "/api/v1/stores/{storeId}/purchase-requests", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Purchase Requests", description = "Remote credit purchase requests")
@PreAuthorize("@creditAuthorization.canManageStore(authentication, #storeId)")
public class StorePurchaseRequestsController {

    private final CreditPurchaseRequestQueryService purchaseRequestQueryService;

    public StorePurchaseRequestsController(CreditPurchaseRequestQueryService purchaseRequestQueryService) {
        this.purchaseRequestQueryService = purchaseRequestQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the purchase requests of the store, newest first; optionally only one status")
    public ResponseEntity<List<CreditPurchaseRequestResource>> getStorePurchaseRequests(
            @PathVariable Long storeId,
            @RequestParam(required = false) PurchaseRequestStatuses status) {
        return ResponseEntity.ok(purchaseRequestQueryService.handle(new GetCreditPurchaseRequestsByStoreIdQuery(storeId))
                .stream()
                .filter(request -> status == null || request.getStatus() == status)
                .map(CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
