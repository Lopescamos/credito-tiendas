package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.CurrencyQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllCurrenciesQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.CurrencyRepository;

import java.util.List;

@Service
public class CurrencyQueryServiceImpl implements CurrencyQueryService {

    private final CurrencyRepository currencyRepository;

    public CurrencyQueryServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> handle(GetAllCurrenciesQuery query) {
        return currencyRepository.findAll();
    }
}
