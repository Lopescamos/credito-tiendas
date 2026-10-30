package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStoreResource(
        @NotBlank @Size(max = 120)
        @Schema(example = "Bodega Don Lucho S.A.C.")
        String legalName,

        @NotBlank @Size(max = 100)
        @Schema(example = "Bodega Don Lucho")
        String tradeName,

        @NotBlank @Size(max = 200)
        @Schema(example = "Av. Los Olivos 123, San Martín de Porres")
        String address,

        @Size(max = 15)
        @Schema(example = "987654321")
        String phone) {
}
