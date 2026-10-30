package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.StorePersistenceEntity;

public interface StorePersistenceRepository extends JpaRepository<StorePersistenceEntity, Long> {
    boolean existsByRuc(Ruc ruc);
}
