package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditProfileCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditProfileQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetAvailableCreditByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditProfileByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditProfilesByUserEmailQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetPurchaseRequestEligibilityQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditProfileResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.PurchaseRequestEligibilityResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.UpdateCreditProfileResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.UpdateCreditProfileStatusResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditProfileResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.PurchaseRequestEligibilityResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.UpdateCreditProfileCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.UpdateCreditProfileStatusCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/credit-profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Credit Profiles", description = "Customer credit accounts and their conditions")
public class CreditProfilesController {

    private final CreditProfileCommandService creditProfileCommandService;
    private final CreditProfileQueryService creditProfileQueryService;

    public CreditProfilesController(CreditProfileCommandService creditProfileCommandService,
                                    CreditProfileQueryService creditProfileQueryService) {
        this.creditProfileCommandService = creditProfileCommandService;
        this.creditProfileQueryService = creditProfileQueryService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get the credit profiles of the authenticated customer, one per store (US-03)")
    public ResponseEntity<List<CreditProfileResource>> getMyCreditProfiles(Authentication authentication) {
        return ResponseEntity.ok(creditProfileQueryService.handle(new GetCreditProfilesByUserEmailQuery(authentication.getName()))
                .stream()
                .map(this::toResource)
                .toList());
    }

    @GetMapping("/{creditProfileId}")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Get a credit profile with its available credit")
    public ResponseEntity<CreditProfileResource> getCreditProfileById(@PathVariable Long creditProfileId) {
        return creditProfileQueryService.handle(new GetCreditProfileByIdQuery(creditProfileId))
                .map(this::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{creditProfileId}")
    @PreAuthorize("@creditAuthorization.canManageProfile(authentication, #creditProfileId)")
    @Operation(summary = "Change the agreed conditions; registered sales keep their rate snapshot (US-04)")
    public ResponseEntity<?> updateCreditProfile(@PathVariable Long creditProfileId,
                                                 @Valid @RequestBody UpdateCreditProfileResource resource) {
        var command = UpdateCreditProfileCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                creditProfileCommandService.handle(command), this::toResource, HttpStatus.OK);
    }

    @PatchMapping("/{creditProfileId}/status")
    @PreAuthorize("@creditAuthorization.canManageProfile(authentication, #creditProfileId)")
    @Operation(summary = "Activate or deactivate a credit profile")
    public ResponseEntity<?> updateCreditProfileStatus(@PathVariable Long creditProfileId,
                                                       @Valid @RequestBody UpdateCreditProfileStatusResource resource) {
        var command = UpdateCreditProfileStatusCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                creditProfileCommandService.handle(command), this::toResource, HttpStatus.OK);
    }

    @GetMapping("/{creditProfileId}/purchase-request-eligibility")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Whether the customer may send a remote purchase request, with every failed rule (US-08)")
    public ResponseEntity<PurchaseRequestEligibilityResource> getPurchaseRequestEligibility(@PathVariable Long creditProfileId) {
        return creditProfileQueryService.handle(new GetPurchaseRequestEligibilityQuery(creditProfileId))
                .map(PurchaseRequestEligibilityResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private CreditProfileResource toResource(CreditProfile profile) {
        var available = creditProfileQueryService.handle(new GetAvailableCreditByCreditProfileIdQuery(profile.getId()))
                .orElse(BigDecimal.ZERO);
        return CreditProfileResourceFromEntityAssembler.toResourceFromEntity(profile, available);
    }
}
