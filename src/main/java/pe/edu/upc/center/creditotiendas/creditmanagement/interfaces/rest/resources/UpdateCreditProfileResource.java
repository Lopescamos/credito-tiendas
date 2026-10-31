package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CapitalizationPeriods;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RateTypes;

import java.math.BigDecimal;

/**
 * New agreed conditions. Sales already registered keep their rate snapshot.
 */
public record UpdateCreditProfileResource(
        @NotNull @PositiveOrZero @Digits(integer = 3, fraction = 7)
        @Schema(description = "Annual compensatory rate (TEA or TNA), percentage", example = "45.0000000")
        BigDecimal interestRate,

        @NotNull
        @Schema(example = "EFFECTIVE")
        RateTypes rateType,

        @Schema(description = "Required when rateType is NOMINAL", example = "MONTHLY")
        CapitalizationPeriods capitalizationPeriod,

        @NotNull @PositiveOrZero @Digits(integer = 3, fraction = 7)
        @Schema(description = "Annual late-payment rate, percentage", example = "60.0000000")
        BigDecimal lateInterestRate,

        @NotNull
        @Schema(description = "A NOMINAL late rate capitalizes daily", example = "EFFECTIVE")
        RateTypes lateInterestRateType,

        @NotNull @Positive
        @Schema(description = "See GET /api/v1/currencies", example = "1")
        Long currencyId,

        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        @Schema(example = "1500.00")
        BigDecimal creditLimit,

        @NotNull @Min(1) @Max(3600)
        @Schema(description = "Longest allowed term of a sale, in days", example = "180")
        Integer maxTermDays,

        @NotNull @Min(1) @Max(28)
        @Schema(example = "25")
        Integer cutoffDay,

        @NotNull @Min(1) @Max(28)
        @Schema(example = "5")
        Integer paymentDay) {
}
