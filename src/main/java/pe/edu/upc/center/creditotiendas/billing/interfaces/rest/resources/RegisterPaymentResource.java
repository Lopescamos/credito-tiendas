package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.PaymentMethods;

import java.time.LocalDate;
import java.util.List;

/**
 * Register a payment covering a chosen set of installments of a single credit sale. The amount is derived
 * from the installments at {@code paymentDate}: the client never sends the amount, which prevents it from
 * under-paying.
 */
public record RegisterPaymentResource(
        @NotEmpty
        List<@NotNull Long> installmentIds,

        @NotNull
        @Schema(example = "2026-04-15")
        LocalDate paymentDate,

        @NotNull
        @Schema(example = "CASH")
        PaymentMethods paymentMethod,

        @Schema(example = "YPE-20260415-0001")
        String reference) {
}
