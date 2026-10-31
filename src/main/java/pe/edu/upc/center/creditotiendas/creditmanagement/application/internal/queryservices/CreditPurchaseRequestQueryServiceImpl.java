package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditPurchaseRequestQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditPurchaseRequestsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditPurchaseRequestRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CreditPurchaseRequestQueryServiceImpl implements CreditPurchaseRequestQueryService {

    private final CreditPurchaseRequestRepository purchaseRequestRepository;

    public CreditPurchaseRequestQueryServiceImpl(CreditPurchaseRequestRepository purchaseRequestRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
    }

    @Override
    public Optional<CreditPurchaseRequest> handle(GetCreditPurchaseRequestByIdQuery query) {
        return purchaseRequestRepository.findById(query.purchaseRequestId());
    }

    @Override
    public List<CreditPurchaseRequest> handle(GetCreditPurchaseRequestsByCreditProfileIdQuery query) {
        return purchaseRequestRepository.findAllByCreditProfileId(query.creditProfileId());
    }

    @Override
    public List<CreditPurchaseRequest> handle(GetCreditPurchaseRequestsByStoreIdQuery query) {
        return purchaseRequestRepository.findAllByStoreId(query.storeId());
    }
}
