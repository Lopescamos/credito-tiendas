package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSalesByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CreditSaleQueryServiceImpl implements CreditSaleQueryService {

    private final CreditSaleRepository creditSaleRepository;

    public CreditSaleQueryServiceImpl(CreditSaleRepository creditSaleRepository) {
        this.creditSaleRepository = creditSaleRepository;
    }

    @Override
    public Optional<CreditSale> handle(GetCreditSaleByIdQuery query) {
        return creditSaleRepository.findById(query.creditSaleId());
    }

    @Override
    public List<CreditSale> handle(GetCreditSalesByCreditProfileIdQuery query) {
        return creditSaleRepository.findAllByCreditProfileId(query.creditProfileId());
    }
}
