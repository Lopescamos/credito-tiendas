package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.MeasurementUnitPersistenceEntity;

public interface MeasurementUnitPersistenceRepository extends JpaRepository<MeasurementUnitPersistenceEntity, Long> {
    boolean existsByName(String name);
}
