package pe.edu.upc.center.creditotiendas.billing.application.commandservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.CreateInstallmentScheduleCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.MarkOverdueInstallmentsCommand;

public interface InstallmentCommandService {

    /**
     * @return the number of installments inserted
     */
    int handle(CreateInstallmentScheduleCommand command);

    /**
     * @return the number of installments that were flipped to {@code OVERDUE}
     */
    int handle(MarkOverdueInstallmentsCommand command);
}
