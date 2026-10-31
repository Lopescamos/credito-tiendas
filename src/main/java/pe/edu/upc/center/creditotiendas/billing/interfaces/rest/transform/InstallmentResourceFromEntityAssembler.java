package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.InstallmentResource;

public class InstallmentResourceFromEntityAssembler {
    public static InstallmentResource toResourceFromEntity(Installment installment) {
        return new InstallmentResource(
                installment.getId(), installment.getCreditSaleId(), installment.getNumber(),
                installment.getDueDate(), installment.getCapital(), installment.getInterest(),
                installment.getFixedInstallment(), installment.getRemainingBalance(),
                installment.getStatus().name(), installment.getPaymentDate());
    }
}
