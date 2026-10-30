package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.MeasurementUnitRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers.MeasurementUnitPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories.MeasurementUnitPersistenceRepository;

import java.util.List;

@Repository
public class MeasurementUnitRepositoryImpl implements MeasurementUnitRepository {

    private final MeasurementUnitPersistenceRepository measurementUnitPersistenceRepository;

    public MeasurementUnitRepositoryImpl(MeasurementUnitPersistenceRepository measurementUnitPersistenceRepository) {
        this.measurementUnitPersistenceRepository = measurementUnitPersistenceRepository;
    }

    @Override
    public List<MeasurementUnit> findAll() {
        return measurementUnitPersistenceRepository.findAll().stream()
                .map(MeasurementUnitPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return measurementUnitPersistenceRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return measurementUnitPersistenceRepository.existsByName(name);
    }

    @Override
    public MeasurementUnit save(MeasurementUnit measurementUnit) {
        var saved = measurementUnitPersistenceRepository.save(
                MeasurementUnitPersistenceAssembler.toPersistenceFromDomain(measurementUnit));
        return MeasurementUnitPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
