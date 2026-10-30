package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.MeasurementUnitQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllMeasurementUnitsQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.MeasurementUnitResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.MeasurementUnitResourceFromEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/measurement-units", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Measurement Units", description = "Measurement unit catalog")
@PreAuthorize("isAuthenticated()")
public class MeasurementUnitsController {

    private final MeasurementUnitQueryService measurementUnitQueryService;

    public MeasurementUnitsController(MeasurementUnitQueryService measurementUnitQueryService) {
        this.measurementUnitQueryService = measurementUnitQueryService;
    }

    @GetMapping
    @Operation(summary = "Get all measurement units")
    public ResponseEntity<List<MeasurementUnitResource>> getAllMeasurementUnits() {
        return ResponseEntity.ok(measurementUnitQueryService.handle(new GetAllMeasurementUnitsQuery()).stream()
                .map(MeasurementUnitResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
