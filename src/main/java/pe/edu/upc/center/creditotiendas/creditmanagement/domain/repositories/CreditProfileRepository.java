package pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link CreditProfile} aggregate.
 */
public interface CreditProfileRepository {
    Optional<CreditProfile> findById(Long id);
    List<CreditProfile> findAllByStoreId(Long storeId);
    List<CreditProfile> findAllByUserId(Long userId);
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);
    CreditProfile save(CreditProfile creditProfile);
}
