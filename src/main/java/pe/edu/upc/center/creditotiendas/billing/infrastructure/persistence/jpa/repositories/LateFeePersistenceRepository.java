package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.LateFeePersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LateFeePersistenceRepository extends JpaRepository<LateFeePersistenceEntity, Long> {

    List<LateFeePersistenceEntity> findAllByInstallmentIdOrderByCalculatedAtDesc(Long installmentId);

    /**
     * Sum of amounts of the newest late fee per installment of the profile, calculated inside the given range.
     * Returns 0 when there are no rows (COALESCE).
     */
    @Query("""
            select coalesce(sum(f.amount), 0) from LateFeePersistenceEntity f
            where f.installment.creditProfileId = :profileId
              and f.calculatedAt >= :fromInstant and f.calculatedAt < :toInstant
              and f.calculatedAt = (
                  select max(f2.calculatedAt) from LateFeePersistenceEntity f2
                  where f2.installment.id = f.installment.id
                    and f2.calculatedAt >= :fromInstant and f2.calculatedAt < :toInstant)""")
    BigDecimal sumLatestByProfileIdInRange(@Param("profileId") Long profileId,
                                           @Param("fromInstant") Instant fromInstant,
                                           @Param("toInstant") Instant toInstant);
}
