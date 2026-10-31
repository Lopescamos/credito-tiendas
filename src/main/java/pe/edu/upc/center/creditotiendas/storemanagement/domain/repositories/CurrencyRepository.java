package pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link Currency} aggregate.
 */
public interface CurrencyRepository {
    Optional<Currency> findById(Long id);
    List<Currency> findAll();
    boolean existsByCode(String code);
    Currency save(Currency currency);
}
