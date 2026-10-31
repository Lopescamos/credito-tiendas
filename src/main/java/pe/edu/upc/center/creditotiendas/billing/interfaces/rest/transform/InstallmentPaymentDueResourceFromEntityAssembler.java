package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentPaymentDue;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.InstallmentPaymentDueResource;

public class InstallmentPaymentDueResourceFromEntityAssembler {
    public static InstallmentPaymentDueResource toResourceFromEntity(InstallmentPaymentDue due) {
        return new InstallmentPaymentDueResource(
                due.installmentId(), due.number(), due.dueDate(), due.daysLate(),
                due.lateFee(), due.interest(), due.capital(), due.total());
    }
}
