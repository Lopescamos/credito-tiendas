package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateCreditProfileStatusResource(
        @NotNull
        @Schema(example = "false")
        Boolean active) {
}
