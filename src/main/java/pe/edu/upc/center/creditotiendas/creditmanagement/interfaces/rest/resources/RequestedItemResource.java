package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RequestedItemResource(
        @NotNull @Positive
        @Schema(example = "1")
        Long productId,

        @NotNull @Positive @Digits(integer = 8, fraction = 2)
        @Schema(example = "2")
        BigDecimal quantity) {
}
