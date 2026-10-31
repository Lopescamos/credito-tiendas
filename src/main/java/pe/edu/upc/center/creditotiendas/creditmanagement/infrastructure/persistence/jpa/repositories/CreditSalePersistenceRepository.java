package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditSalePersistenceEntity;

import java.util.List;

public interface CreditSalePersistenceRepository extends JpaRepository<CreditSalePersistenceEntity, Long> {
    List<CreditSalePersistenceEntity> findAllByCreditProfileIdOrderBySaleDateDescIdDesc(Long creditProfileId);
}
