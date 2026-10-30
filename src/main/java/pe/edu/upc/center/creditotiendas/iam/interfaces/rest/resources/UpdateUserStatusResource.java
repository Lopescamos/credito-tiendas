package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusResource(
        @NotNull
        @Schema(example = "false")
        Boolean active) {
}
