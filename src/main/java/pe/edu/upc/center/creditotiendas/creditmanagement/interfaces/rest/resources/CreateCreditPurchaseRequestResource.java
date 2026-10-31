package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCreditPurchaseRequestResource(
        @NotEmpty
        List<@Valid @NotNull RequestedItemResource> items) {
}
