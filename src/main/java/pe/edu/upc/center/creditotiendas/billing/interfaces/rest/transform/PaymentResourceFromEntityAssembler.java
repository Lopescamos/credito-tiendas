package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.PaymentAllocationResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {
    public static PaymentResource toResourceFromEntity(Payment payment) {
        return new PaymentResource(
                payment.getId(), payment.getCreditProfileId(), payment.getTotalAmount(),
                payment.getPaymentDate(), payment.getPaymentMethod().name(), payment.getReference(),
                payment.getRegisteredAt(),
                payment.getAllocations().stream()
                        .map(a -> new PaymentAllocationResource(a.getInstallmentId(),
                                a.getLateFeeAmount(), a.getInterestAmount(), a.getCapitalAmount()))
                        .toList());
    }
}
