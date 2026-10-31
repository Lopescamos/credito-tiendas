package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectCreditPurchaseRequestResource(
        @NotBlank @Size(max = 300)
        @Schema(description = "Shown to the customer", example = "Primero cancela tu deuda pendiente")
        String reason) {
}
