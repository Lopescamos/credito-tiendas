package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;

import java.util.List;

/**
 * Body to register or simulate a credit sale (US-02). Prices come from the catalog, not from the client.
 */
public record CreditSaleRequestResource(
        @NotNull
        @Schema(example = "INSTALLMENTS")
        SaleModalities modality,

        @NotNull @Min(1) @Max(120)
        @Schema(description = "1 for SINGLE_PAYMENT, 2 or more for INSTALLMENTS", example = "3")
        Integer installmentCount,

        @NotNull @Min(0)
        @Schema(description = "Days of total grace before the first 30-day period", example = "0")
        Integer graceDays,

        @NotEmpty
        List<@Valid @NotNull RequestedItemResource> items) {
}
