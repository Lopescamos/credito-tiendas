package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditProfileCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditProfileQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetAvailableCreditByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditProfileByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditProfilesByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreateCreditProfileResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditProfileResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreateCreditProfileCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditProfileResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Customers with credit in a store (US-04). Only store admins linked to the store.
 */
@RestController
@RequestMapping(value = "/api/v1/stores/{storeId}/credit-profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Store Credit Profiles", description = "Customer credit profiles of a store")
@PreAuthorize("@creditAuthorization.canManageStore(authentication, #storeId)")
public class StoreCreditProfilesController {

    private final CreditProfileCommandService creditProfileCommandService;
    private final CreditProfileQueryService creditProfileQueryService;

    public StoreCreditProfilesController(CreditProfileCommandService creditProfileCommandService,
                                         CreditProfileQueryService creditProfileQueryService) {
        this.creditProfileCommandService = creditProfileCommandService;
        this.creditProfileQueryService = creditProfileQueryService;
    }

    @PostMapping
    @Operation(summary = "Open a credit profile for a CLIENT user with the agreed conditions")
    public ResponseEntity<?> createCreditProfile(@PathVariable Long storeId,
                                                 @Valid @RequestBody CreateCreditProfileResource resource) {
        var command = CreateCreditProfileCommandFromResourceAssembler.toCommandFromResource(storeId, resource);
        var result = creditProfileCommandService.handle(command)
                .flatMap(profileId -> creditProfileQueryService.handle(new GetCreditProfileByIdQuery(profileId))
                        .<Result<CreditProfile, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("CreditProfile", profileId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, this::toResource, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get the credit profiles of the store")
    public ResponseEntity<List<CreditProfileResource>> getStoreCreditProfiles(@PathVariable Long storeId) {
        return ResponseEntity.ok(creditProfileQueryService.handle(new GetCreditProfilesByStoreIdQuery(storeId)).stream()
                .map(this::toResource)
                .toList());
    }

    private CreditProfileResource toResource(CreditProfile profile) {
        var available = creditProfileQueryService.handle(new GetAvailableCreditByCreditProfileIdQuery(profile.getId()))
                .orElse(BigDecimal.ZERO);
        return CreditProfileResourceFromEntityAssembler.toResourceFromEntity(profile, available);
    }
}
