package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusResource(
        @NotNull
        @Schema(example = "false")
        Boolean active) {
}
