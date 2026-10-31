package pe.edu.upc.center.creditotiendas.creditmanagement.application.acl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CreditSaleStatuses;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl.CreditManagementContextFacade;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl.CreditProfileSummary;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CreditManagementContextFacadeImpl implements CreditManagementContextFacade {

    private final CreditProfileRepository creditProfileRepository;
    private final CreditSaleRepository creditSaleRepository;

    public CreditManagementContextFacadeImpl(CreditProfileRepository creditProfileRepository,
                                             CreditSaleRepository creditSaleRepository) {
        this.creditProfileRepository = creditProfileRepository;
        this.creditSaleRepository = creditSaleRepository;
    }

    @Override
    public Optional<CreditProfileSummary> fetchProfileById(Long creditProfileId) {
        return creditProfileRepository.findById(creditProfileId).map(this::toSummary);
    }

    @Override
    public List<Long> fetchActiveProfileIdsByCutoffDay(int cutoffDay) {
        return creditProfileRepository.findAllActiveByCutoffDay(cutoffDay).stream()
                .map(CreditProfile::getId)
                .toList();
    }

    @Override
    public boolean updateCreditSaleStatus(Long creditSaleId, String newStatus) {
        CreditSaleStatuses status;
        try {
            status = CreditSaleStatuses.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown credit sale status '{}' sent by Billing", newStatus);
            return false;
        }
        var sale = creditSaleRepository.findById(creditSaleId);
        if (sale.isEmpty()) return false;
        sale.get().setStatus(status);
        creditSaleRepository.save(sale.get());
        return true;
    }

    private CreditProfileSummary toSummary(CreditProfile profile) {
        return new CreditProfileSummary(profile.getId(), profile.getUserId(), profile.getStoreId(),
                profile.getCurrencyId(), profile.getCutoffDay(), profile.getPaymentDay());
    }
}
