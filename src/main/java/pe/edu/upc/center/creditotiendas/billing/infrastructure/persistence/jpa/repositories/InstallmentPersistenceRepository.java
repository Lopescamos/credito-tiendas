package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.InstallmentPersistenceEntity;

import java.time.LocalDate;
import java.util.List;

public interface InstallmentPersistenceRepository extends JpaRepository<InstallmentPersistenceEntity, Long> {

    List<InstallmentPersistenceEntity> findAllByCreditSaleIdOrderByNumberAsc(Long creditSaleId);

    @Query("""
            select i from InstallmentPersistenceEntity i
            where i.creditProfileId = :profileId and i.status in (
                pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses.PENDING,
                pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses.OVERDUE)
            order by i.dueDate asc, i.number asc""")
    List<InstallmentPersistenceEntity> findAllUnpaidByProfileId(@Param("profileId") Long profileId);

    List<InstallmentPersistenceEntity> findAllByStatusAndDueDateBefore(InstallmentStatuses status, LocalDate asOf);

    List<InstallmentPersistenceEntity> findAllByCreditProfileIdAndDueDateBetweenOrderByDueDateAsc(
            Long creditProfileId, LocalDate from, LocalDate to);

    @Query("""
            select i from InstallmentPersistenceEntity i
            where i.creditProfileId in (
                select p.id from pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditProfilePersistenceEntity p
                where p.storeId = :storeId)
              and i.dueDate <= :asOf and i.status in (
                pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses.PENDING,
                pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses.OVERDUE)
            order by i.dueDate asc""")
    List<InstallmentPersistenceEntity> findAllDueByStoreIdAndDate(@Param("storeId") Long storeId, @Param("asOf") LocalDate asOf);

    boolean existsByCreditSaleIdAndStatusIn(Long creditSaleId, List<InstallmentStatuses> statuses);
}
