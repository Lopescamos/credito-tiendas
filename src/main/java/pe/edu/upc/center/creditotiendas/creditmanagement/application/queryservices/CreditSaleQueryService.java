package pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSalesByCreditProfileIdQuery;

import java.util.List;
import java.util.Optional;

public interface CreditSaleQueryService {
    Optional<CreditSale> handle(GetCreditSaleByIdQuery query);
    List<CreditSale> handle(GetCreditSalesByCreditProfileIdQuery query);
}
