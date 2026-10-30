package pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedMeasurementUnitsCommand;

public interface MeasurementUnitCommandService {
    void handle(SeedMeasurementUnitsCommand command);
}
