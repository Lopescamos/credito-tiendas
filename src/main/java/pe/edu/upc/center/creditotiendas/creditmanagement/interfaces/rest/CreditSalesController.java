package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.PaymentPlanResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.CreditSaleResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform.PaymentPlanResourceFromEntityAssembler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * A single credit sale, for its store admins and its customer.
 */
@RestController
@RequestMapping(value = "/api/v1/credit-sales", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Credit Sales", description = "Credit sales of a customer")
@PreAuthorize("@creditAuthorization.canAccessSale(authentication, #creditSaleId)")
public class CreditSalesController {

    private final CreditSaleQueryService creditSaleQueryService;

    public CreditSalesController(CreditSaleQueryService creditSaleQueryService) {
        this.creditSaleQueryService = creditSaleQueryService;
    }

    @GetMapping("/{creditSaleId}")
    @Operation(summary = "Get a credit sale with its details and rate snapshot")
    public ResponseEntity<CreditSaleResource> getCreditSaleById(@PathVariable Long creditSaleId) {
        return creditSaleQueryService.handle(new GetCreditSaleByIdQuery(creditSaleId))
                .map(CreditSaleResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{creditSaleId}/payment-plan")
    @Operation(summary = "Get the payment plan computed from the sale snapshot, never from current conditions (US-07)")
    public ResponseEntity<PaymentPlanResource> getPaymentPlan(@PathVariable Long creditSaleId) {
        return creditSaleQueryService.handle(new GetCreditSaleByIdQuery(creditSaleId))
                .map(CreditSale::paymentPlan)
                .map(PaymentPlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
