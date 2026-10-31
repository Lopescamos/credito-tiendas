package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.CurrencyRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers.CurrencyPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories.CurrencyPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CurrencyRepositoryImpl implements CurrencyRepository {

    private final CurrencyPersistenceRepository currencyPersistenceRepository;

    public CurrencyRepositoryImpl(CurrencyPersistenceRepository currencyPersistenceRepository) {
        this.currencyPersistenceRepository = currencyPersistenceRepository;
    }

    @Override
    public Optional<Currency> findById(Long id) {
        return currencyPersistenceRepository.findById(id).map(CurrencyPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Currency> findAll() {
        return currencyPersistenceRepository.findAll().stream()
                .map(CurrencyPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return currencyPersistenceRepository.existsByCode(code);
    }

    @Override
    public Currency save(Currency currency) {
        var saved = currencyPersistenceRepository.save(CurrencyPersistenceAssembler.toPersistenceFromDomain(currency));
        return CurrencyPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
