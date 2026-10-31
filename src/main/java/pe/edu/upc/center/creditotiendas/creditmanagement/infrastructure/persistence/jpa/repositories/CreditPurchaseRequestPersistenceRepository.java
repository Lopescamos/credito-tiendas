package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditPurchaseRequestPersistenceEntity;

import java.util.List;

public interface CreditPurchaseRequestPersistenceRepository extends JpaRepository<CreditPurchaseRequestPersistenceEntity, Long> {

    List<CreditPurchaseRequestPersistenceEntity> findAllByCreditProfileIdOrderByRequestedAtDesc(Long creditProfileId);

    @Query("""
            select r from CreditPurchaseRequestPersistenceEntity r
            where r.creditProfileId in (
                select p.id from CreditProfilePersistenceEntity p where p.storeId = :storeId)
            order by r.requestedAt desc""")
    List<CreditPurchaseRequestPersistenceEntity> findAllByStoreId(@Param("storeId") Long storeId);
}
