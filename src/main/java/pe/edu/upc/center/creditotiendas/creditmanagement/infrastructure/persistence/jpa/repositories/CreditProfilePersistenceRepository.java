package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditProfilePersistenceEntity;

import java.util.List;

public interface CreditProfilePersistenceRepository extends JpaRepository<CreditProfilePersistenceEntity, Long> {
    List<CreditProfilePersistenceEntity> findAllByStoreIdOrderByIdAsc(Long storeId);
    List<CreditProfilePersistenceEntity> findAllByUserIdOrderByIdAsc(Long userId);
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);
}
