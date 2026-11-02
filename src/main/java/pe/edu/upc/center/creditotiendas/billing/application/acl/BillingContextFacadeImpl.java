package pe.edu.upc.center.creditotiendas.billing.application.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;
import pe.edu.upc.center.creditotiendas.billing.interfaces.acl.BillingContextFacade;

import java.math.BigDecimal;

@Service
public class BillingContextFacadeImpl implements BillingContextFacade {

    private final InstallmentRepository installmentRepository;

    public BillingContextFacadeImpl(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    public BigDecimal fetchPendingCapitalByCreditProfileId(Long creditProfileId) {
        return installmentRepository.findAllUnpaidByCreditProfileId(creditProfileId).stream()
                .map(Installment::getCapital)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
