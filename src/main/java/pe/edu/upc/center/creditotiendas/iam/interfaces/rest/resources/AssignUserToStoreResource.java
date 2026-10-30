package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignUserToStoreResource(
        @NotNull @Positive
        @Schema(example = "1")
        Long storeId) {
}
