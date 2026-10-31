package pe.edu.upc.center.creditotiendas.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.InstallmentQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsByCreditSaleIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsDueTodayByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.InstallmentResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform.InstallmentResourceFromEntityAssembler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Installment schedule reads for one sale (US-07) and for a store's dashboard (US-05).
 */
@RestController
@RequestMapping(value = "/api/v1", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Installments", description = "Payment schedule")
public class InstallmentsController {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    private final InstallmentQueryService installmentQueryService;

    public InstallmentsController(InstallmentQueryService installmentQueryService) {
        this.installmentQueryService = installmentQueryService;
    }

    @GetMapping("/credit-sales/{creditSaleId}/installments")
    @PreAuthorize("@creditAuthorization.canAccessSale(authentication, #creditSaleId)")
    @Operation(summary = "Real installments of the sale with their current state (US-07)")
    public ResponseEntity<List<InstallmentResource>> getInstallments(@PathVariable Long creditSaleId) {
        return ResponseEntity.ok(installmentQueryService.handle(new GetInstallmentsByCreditSaleIdQuery(creditSaleId))
                .stream()
                .map(InstallmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @GetMapping("/stores/{storeId}/installments-due")
    @PreAuthorize("@creditAuthorization.canManageStore(authentication, #storeId)")
    @Operation(summary = "Installments due or overdue today in the store (US-05 dashboard)")
    public ResponseEntity<List<InstallmentResource>> getDueTodayByStore(@PathVariable Long storeId) {
        var today = LocalDate.now(BUSINESS_ZONE);
        return ResponseEntity.ok(installmentQueryService.handle(new GetInstallmentsDueTodayByStoreIdQuery(storeId, today))
                .stream()
                .map(InstallmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
