package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.AccountStatementPersistenceEntity;

import java.util.List;
import java.util.Optional;

public interface AccountStatementPersistenceRepository extends JpaRepository<AccountStatementPersistenceEntity, Long> {

    List<AccountStatementPersistenceEntity> findAllByCreditProfileIdOrderByPeriodDesc(Long creditProfileId);

    Optional<AccountStatementPersistenceEntity> findFirstByCreditProfileIdAndPeriodLessThanOrderByPeriodDesc(
            Long creditProfileId, String period);

    boolean existsByCreditProfileIdAndPeriod(Long creditProfileId, String period);
}
