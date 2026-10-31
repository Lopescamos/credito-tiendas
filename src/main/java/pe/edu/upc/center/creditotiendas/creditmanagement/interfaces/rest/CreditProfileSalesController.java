package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditSaleCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSalesByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditSaleResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditSaleSimulationResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.RegisterCreditSaleCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.SimulateCreditSaleCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Credit sales of one customer (US-02).
 */
@RestController
@RequestMapping(value = "/api/v1/credit-profiles/{creditProfileId}", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Credit Sales", description = "Credit sales of a customer")
public class CreditProfileSalesController {

    private final CreditSaleCommandService creditSaleCommandService;
    private final CreditSaleQueryService creditSaleQueryService;

    public CreditProfileSalesController(CreditSaleCommandService creditSaleCommandService,
                                        CreditSaleQueryService creditSaleQueryService) {
        this.creditSaleCommandService = creditSaleCommandService;
        this.creditSaleQueryService = creditSaleQueryService;
    }

    @PostMapping("/credit-sale-simulations")
    @PreAuthorize("@creditAuthorization.canManageProfile(authentication, #creditProfileId)")
    @Operation(summary = "Preview a credit sale and its payment plan without registering it")
    public ResponseEntity<?> simulateCreditSale(@PathVariable Long creditProfileId,
                                                @Valid @RequestBody CreditSaleRequestResource resource) {
        var command = SimulateCreditSaleCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                creditSaleCommandService.handle(command),
                CreditSaleSimulationResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PostMapping("/credit-sales")
    @PreAuthorize("@creditAuthorization.canManageProfile(authentication, #creditProfileId)")
    @Operation(summary = "Register a credit sale; it copies the current rates of the profile (snapshot)")
    public ResponseEntity<?> registerCreditSale(@PathVariable Long creditProfileId,
                                                @Valid @RequestBody CreditSaleRequestResource resource) {
        var command = RegisterCreditSaleCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        var result = creditSaleCommandService.handle(command)
                .flatMap(saleId -> creditSaleQueryService.handle(new GetCreditSaleByIdQuery(saleId))
                        .<Result<CreditSale, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("CreditSale", saleId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, CreditSaleResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/credit-sales")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Get the credit sales of the customer, newest first")
    public ResponseEntity<List<CreditSaleResource>> getCreditSales(@PathVariable Long creditProfileId) {
        return ResponseEntity.ok(creditSaleQueryService.handle(new GetCreditSalesByCreditProfileIdQuery(creditProfileId))
                .stream()
                .map(CreditSaleResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
