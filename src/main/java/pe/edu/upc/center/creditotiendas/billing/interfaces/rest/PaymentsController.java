package pe.edu.upc.center.creditotiendas.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.PaymentCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.InstallmentQueryService;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.PaymentQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentDueByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.InstallmentPaymentDueResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.PaymentResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.RegisterPaymentResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform.InstallmentPaymentDueResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform.PaymentResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform.RegisterPaymentCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Payment registration and lookup for a credit profile (US-05, US-10).
 */
@RestController
@RequestMapping(value = "/api/v1/credit-profiles/{creditProfileId}", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Payments", description = "Payments and amounts owed")
public class PaymentsController {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final InstallmentQueryService installmentQueryService;

    public PaymentsController(PaymentCommandService paymentCommandService, PaymentQueryService paymentQueryService,
                              InstallmentQueryService installmentQueryService) {
        this.paymentCommandService = paymentCommandService;
        this.paymentQueryService = paymentQueryService;
        this.installmentQueryService = installmentQueryService;
    }

    @GetMapping("/payment-due")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Amount owed today, broken down by installment (US-05 desglose, US-10 total exigible)")
    public ResponseEntity<List<InstallmentPaymentDueResource>> getPaymentDue(
            @PathVariable Long creditProfileId,
            @RequestParam(required = false) List<Long> installmentIds) {
        var today = LocalDate.now(BUSINESS_ZONE);
        var dues = installmentQueryService.handle(new GetPaymentDueByCreditProfileIdQuery(creditProfileId, today, installmentIds));
        return ResponseEntity.ok(dues.stream()
                .map(InstallmentPaymentDueResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PostMapping("/payments")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Register a payment for the selected installments; the amount comes from the server")
    public ResponseEntity<?> registerPayment(@PathVariable Long creditProfileId,
                                             @Valid @RequestBody RegisterPaymentResource resource) {
        var command = RegisterPaymentCommandFromResourceAssembler.toCommandFromResource(creditProfileId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                paymentCommandService.handle(command),
                PaymentResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @GetMapping("/payments")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Payment history of the profile, newest first")
    public ResponseEntity<List<PaymentResource>> getPayments(@PathVariable Long creditProfileId) {
        return ResponseEntity.ok(paymentQueryService.handle(new GetPaymentsByCreditProfileIdQuery(creditProfileId)).stream()
                .map(PaymentResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
