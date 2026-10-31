package pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link CreditPurchaseRequest} aggregate, including its details.
 */
public interface CreditPurchaseRequestRepository {
    Optional<CreditPurchaseRequest> findById(Long id);
    List<CreditPurchaseRequest> findAllByCreditProfileId(Long creditProfileId);
    List<CreditPurchaseRequest> findAllByStoreId(Long storeId);
    CreditPurchaseRequest save(CreditPurchaseRequest request);
}
