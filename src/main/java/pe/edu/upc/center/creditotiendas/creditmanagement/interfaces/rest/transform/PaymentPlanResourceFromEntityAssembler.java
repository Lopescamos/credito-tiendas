package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentPlan;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.PaymentPlanResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.ScheduledInstallmentResource;

public class PaymentPlanResourceFromEntityAssembler {
    public static PaymentPlanResource toResourceFromEntity(PaymentPlan plan) {
        return new PaymentPlanResource(
                plan.financedCapital(),
                plan.fixedInstallment(),
                plan.totalInterest(),
                plan.totalToPay(),
                plan.installments().stream()
                        .map(installment -> new ScheduledInstallmentResource(
                                installment.number(), installment.dueDate(), installment.interest(),
                                installment.capital(), installment.amount(), installment.remainingBalance()))
                        .toList());
    }
}
