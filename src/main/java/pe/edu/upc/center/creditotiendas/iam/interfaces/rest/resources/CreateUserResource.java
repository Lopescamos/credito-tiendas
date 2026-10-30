package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;

public record CreateUserResource(
        @NotBlank @Email @Size(max = 100)
        @Schema(example = "maria.quispe@gmail.com")
        String email,

        @NotBlank @Size(max = 60)
        @Schema(example = "María")
        String firstName,

        @NotBlank @Size(max = 60)
        @Schema(example = "Quispe")
        String lastName,

        @NotBlank @Size(min = 8, max = 72)
        @Schema(example = "Cambiar.2026")
        String password,

        @NotNull
        @Schema(description = "STORE_ADMIN or CLIENT", example = "STORE_ADMIN")
        Roles role) {
}
