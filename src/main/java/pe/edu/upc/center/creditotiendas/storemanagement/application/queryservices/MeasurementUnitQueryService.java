package pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllMeasurementUnitsQuery;

import java.util.List;

public interface MeasurementUnitQueryService {
    List<MeasurementUnit> handle(GetAllMeasurementUnitsQuery query);
}
