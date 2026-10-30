package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.ProductTypes;

import java.math.BigDecimal;

public record CreateProductResource(
        @NotBlank @Size(max = 100)
        @Schema(example = "Arroz Costeño 5 kg")
        String name,

        @Size(max = 300)
        @Schema(example = "Arroz extra graneado")
        String description,

        @NotNull
        @Schema(example = "PRODUCT")
        ProductTypes type,

        @Size(max = 100)
        @Schema(example = "Distribuidora Lima Norte")
        String supplier,

        @Size(max = 60)
        @Schema(example = "Costeño")
        String brand,

        @Positive
        @Schema(description = "Optional; see GET /api/v1/measurement-units", example = "1")
        Long measurementUnitId,

        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        @Schema(example = "24.50")
        BigDecimal cashPrice,

        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        @Schema(example = "26.00")
        BigDecimal creditPrice) {
}
