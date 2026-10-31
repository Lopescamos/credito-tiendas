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
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreateCreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreateCreditPurchaseRequestCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditPurchaseRequestResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Remote credit purchase requests of one customer (US-08, US-09).
 */
@RestController
@RequestMapping(value = "/api/v1/credit-profiles/{creditProfileId}/purchase-requests", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Purchase Requests", description = "Remote credit purchase requests")
public class CreditProfilePurchaseRequestsController {

    private final CreditPurchaseRequestCommandService purchaseRequestCommandService;
    private final CreditPurchaseRequestQueryService purchaseRequestQueryService;

    public CreditProfilePurchaseRequestsController(CreditPurchaseRequestCommandService purchaseRequestCommandService,
                                                   CreditPurchaseRequestQueryService purchaseRequestQueryService) {
        this.purchaseRequestCommandService = purchaseRequestCommandService;
        this.purchaseRequestQueryService = purchaseRequestQueryService;
    }

    @PostMapping
    @PreAuthorize("@creditAuthorization.ownsProfile(authentication, #creditProfileId)")
    @Operation(summary = "Send a purchase request; rejected with 422 if the customer is not eligible (US-08)")
    public ResponseEntity<?> createPurchaseRequest(@PathVariable Long creditProfileId,
                                                   @Valid @RequestBody CreateCreditPurchaseRequestResource resource) {
        var command = CreateCreditPurchaseRequestCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        var result = purchaseRequestCommandService.handle(command)
                .flatMap(requestId -> purchaseRequestQueryService.handle(new GetCreditPurchaseRequestByIdQuery(requestId))
                        .<Result<CreditPurchaseRequest, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("CreditPurchaseRequest", requestId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Get the purchase requests of the customer with their status (US-09)")
    public ResponseEntity<List<CreditPurchaseRequestResource>> getPurchaseRequests(@PathVariable Long creditProfileId) {
        return ResponseEntity.ok(purchaseRequestQueryService
                .handle(new GetCreditPurchaseRequestsByCreditProfileIdQuery(creditProfileId)).stream()
                .map(CreditPurchaseRequestResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
