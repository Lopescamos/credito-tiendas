package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.CurrencyQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllCurrenciesQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.CurrencyResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.CurrencyResourceFromEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/currencies", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Currencies", description = "Currency catalog")
@PreAuthorize("isAuthenticated()")
public class CurrenciesController {

    private final CurrencyQueryService currencyQueryService;

    public CurrenciesController(CurrencyQueryService currencyQueryService) {
        this.currencyQueryService = currencyQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all currencies")
    public ResponseEntity<List<CurrencyResource>> getAllCurrencies() {
        return ResponseEntity.ok(currencyQueryService.handle(new GetAllCurrenciesQuery()).stream()
                .map(CurrencyResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
