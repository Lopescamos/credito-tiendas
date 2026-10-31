package pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllCurrenciesQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetCurrencyByIdQuery;

import java.util.List;
import java.util.Optional;

public interface CurrencyQueryService {
    Optional<Currency> handle(GetCurrencyByIdQuery query);
    List<Currency> handle(GetAllCurrenciesQuery query);
}
