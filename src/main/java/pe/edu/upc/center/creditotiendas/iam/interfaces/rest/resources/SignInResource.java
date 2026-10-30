package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInResource(
        @NotBlank @Email
        @Schema(example = "admin@creditotiendas.pe")
        String email,

        @NotBlank
        @Schema(example = "********")
        String password) {
}
