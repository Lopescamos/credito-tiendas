package pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link CreditSale} aggregate, including its details.
 */
public interface CreditSaleRepository {
    Optional<CreditSale> findById(Long id);
    List<CreditSale> findAllByCreditProfileId(Long creditProfileId);
    CreditSale save(CreditSale creditSale);
}
