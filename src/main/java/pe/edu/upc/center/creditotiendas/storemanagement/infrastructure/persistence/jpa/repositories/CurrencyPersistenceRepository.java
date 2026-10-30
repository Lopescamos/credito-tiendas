package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.CurrencyPersistenceEntity;

public interface CurrencyPersistenceRepository extends JpaRepository<CurrencyPersistenceEntity, Long> {
    boolean existsByCode(String code);
}
