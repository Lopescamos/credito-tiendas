package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.MeasurementUnitCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedMeasurementUnitsCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.MeasurementUnitRepository;

import java.util.Map;

@Service
public class MeasurementUnitCommandServiceImpl implements MeasurementUnitCommandService {

    /** Name → abbreviation of the units common in small Peruvian stores. */
    private static final Map<String, String> DEFAULT_UNITS = Map.of(
            "Unidad", "und",
            "Kilogramo", "kg",
            "Gramo", "g",
            "Litro", "L",
            "Mililitro", "ml",
            "Metro", "m",
            "Paquete", "paq",
            "Caja", "caja",
            "Docena", "doc",
            "Botella", "bot");

    private final MeasurementUnitRepository measurementUnitRepository;

    public MeasurementUnitCommandServiceImpl(MeasurementUnitRepository measurementUnitRepository) {
        this.measurementUnitRepository = measurementUnitRepository;
    }

    /**
     * Inserts only the missing units, matched by name.
     */
    @Override
    public void handle(SeedMeasurementUnitsCommand command) {
        DEFAULT_UNITS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(unit -> !measurementUnitRepository.existsByName(unit.getKey()))
                .forEach(unit -> measurementUnitRepository.save(new MeasurementUnit(unit.getKey(), unit.getValue())));
    }
}
