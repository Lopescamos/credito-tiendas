package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RequestedItem;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;

import java.util.List;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Registers a credit sale with the current conditions of the profile (US-02).
 *
 * @param installmentCount 1 for SINGLE_PAYMENT, 2 or more for INSTALLMENTS
 * @param graceDays        days of total grace before the first 30-day period; 0 for none
 */
public record RegisterCreditSaleCommand(Long creditProfileId, SaleModalities modality, Integer installmentCount,
                    Integer graceDays, List<RequestedItem> items) {
    public RegisterCreditSaleCommand {
        creditProfileId = requireId(creditProfileId, "creditProfileId");
        requireNonNull(modality, "modality");
        installmentCount = requireRange(installmentCount, "installmentCount", 1, MAX_INSTALLMENTS);
        requireInstallmentsMatchModality(modality, installmentCount);
        graceDays = requireRange(graceDays, "graceDays", 0, MAX_TERM_DAYS);
        items = requireItems(items);
    }
}
