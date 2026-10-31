package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.PaymentPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentPersistenceRepository extends JpaRepository<PaymentPersistenceEntity, Long> {

    List<PaymentPersistenceEntity> findAllByCreditProfileIdOrderByPaymentDateDescIdDesc(Long creditProfileId);

    @Query("""
            select coalesce(sum(p.totalAmount), 0) from PaymentPersistenceEntity p
            where p.creditProfileId = :profileId and p.paymentDate >= :from and p.paymentDate <= :to""")
    BigDecimal sumAmountByProfileIdInRange(@Param("profileId") Long profileId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);
}
