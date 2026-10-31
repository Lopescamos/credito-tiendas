package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditPurchaseRequestCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditPurchaseRequestQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.ApproveCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.RejectCreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditPurchaseRequestResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.RejectCreditPurchaseRequestCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * A single purchase request: read by its customer or store admins, answered by store admins (US-12).
 */
@RestController
@RequestMapping(value = "/api/v1/purchase-requests", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Purchase Requests", description = "Remote credit purchase requests")
public class PurchaseRequestsController {

    private final CreditPurchaseRequestCommandService purchaseRequestCommandService;
    private final CreditPurchaseRequestQueryService purchaseRequestQueryService;

    public PurchaseRequestsController(CreditPurchaseRequestCommandService purchaseRequestCommandService,
                                      CreditPurchaseRequestQueryService purchaseRequestQueryService) {
        this.purchaseRequestCommandService = purchaseRequestCommandService;
        this.purchaseRequestQueryService = purchaseRequestQueryService;
    }

    @GetMapping("/{purchaseRequestId}")
    @PreAuthorize("@creditAuthorization.canAccessRequest(authentication, #purchaseRequestId)")
    @Operation(summary = "Get a purchase request; if approved it links to the generated sale (US-09)")
    public ResponseEntity<CreditPurchaseRequestResource> getPurchaseRequestById(@PathVariable Long purchaseRequestId) {
        return purchaseRequestQueryService.handle(new GetCreditPurchaseRequestByIdQuery(purchaseRequestId))
                .map(CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{purchaseRequestId}/approval")
    @PreAuthorize("@creditAuthorization.canManageRequest(authentication, #purchaseRequestId)")
    @Operation(summary = "Approve a pending request; the credit sale is generated automatically")
    public ResponseEntity<?> approvePurchaseRequest(@PathVariable Long purchaseRequestId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                purchaseRequestCommandService.handle(new ApproveCreditPurchaseRequestCommand(purchaseRequestId)),
                CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PostMapping("/{purchaseRequestId}/rejection")
    @PreAuthorize("@creditAuthorization.canManageRequest(authentication, #purchaseRequestId)")
    @Operation(summary = "Reject a pending request with a mandatory reason shown to the customer")
    public ResponseEntity<?> rejectPurchaseRequest(@PathVariable Long purchaseRequestId,
                                                   @Valid @RequestBody RejectCreditPurchaseRequestResource resource) {
        var command = RejectCreditPurchaseRequestCommandFromResourceAssembler.toCommandFromResource(purchaseRequestId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                purchaseRequestCommandService.handle(command),
                CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
