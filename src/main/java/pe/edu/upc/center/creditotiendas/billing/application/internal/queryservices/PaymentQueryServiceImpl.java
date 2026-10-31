package pe.edu.upc.center.creditotiendas.billing.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.PaymentQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.PaymentRepository;

import java.util.List;

@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentQueryServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<Payment> handle(GetPaymentsByCreditProfileIdQuery query) {
        return paymentRepository.findAllByCreditProfileId(query.creditProfileId());
    }
}
