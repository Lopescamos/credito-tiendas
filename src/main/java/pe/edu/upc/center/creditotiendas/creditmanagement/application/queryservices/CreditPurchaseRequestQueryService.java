package pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByStoreIdQuery;

import java.util.List;
import java.util.Optional;

public interface CreditPurchaseRequestQueryService {
    Optional<CreditPurchaseRequest> handle(GetCreditPurchaseRequestByIdQuery query);
    List<CreditPurchaseRequest> handle(GetCreditPurchaseRequestsByCreditProfileIdQuery query);
    List<CreditPurchaseRequest> handle(GetCreditPurchaseRequestsByStoreIdQuery query);
}
