package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.MeasurementUnitQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllMeasurementUnitsQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.MeasurementUnitRepository;

import java.util.List;

@Service
public class MeasurementUnitQueryServiceImpl implements MeasurementUnitQueryService {

    private final MeasurementUnitRepository measurementUnitRepository;

    public MeasurementUnitQueryServiceImpl(MeasurementUnitRepository measurementUnitRepository) {
        this.measurementUnitRepository = measurementUnitRepository;
    }

    @Override
    public List<MeasurementUnit> handle(GetAllMeasurementUnitsQuery query) {
        return measurementUnitRepository.findAll();
    }
}
