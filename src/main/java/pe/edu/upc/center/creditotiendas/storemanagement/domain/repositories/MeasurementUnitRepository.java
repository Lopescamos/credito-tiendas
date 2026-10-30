package pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;

import java.util.List;

/**
 * Persistence port of the {@link MeasurementUnit} aggregate.
 */
public interface MeasurementUnitRepository {
    List<MeasurementUnit> findAll();
    boolean existsById(Long id);
    boolean existsByName(String name);
    MeasurementUnit save(MeasurementUnit measurementUnit);
}
