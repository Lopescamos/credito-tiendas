package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserResource(
        @NotBlank @Size(max = 60)
        @Schema(example = "María")
        String firstName,

        @NotBlank @Size(max = 60)
        @Schema(example = "Quispe Huamán")
        String lastName) {
}
