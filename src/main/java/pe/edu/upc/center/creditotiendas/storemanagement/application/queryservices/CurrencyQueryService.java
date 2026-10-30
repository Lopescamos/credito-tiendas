package pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllCurrenciesQuery;

import java.util.List;

public interface CurrencyQueryService {
    List<Currency> handle(GetAllCurrenciesQuery query);
}
