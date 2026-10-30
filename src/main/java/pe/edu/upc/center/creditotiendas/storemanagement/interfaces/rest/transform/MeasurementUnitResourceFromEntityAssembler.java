package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.MeasurementUnitResource;

public class MeasurementUnitResourceFromEntityAssembler {
    public static MeasurementUnitResource toResourceFromEntity(MeasurementUnit unit) {
        return new MeasurementUnitResource(unit.getId(), unit.getName(), unit.getAbbreviation());
    }
}
