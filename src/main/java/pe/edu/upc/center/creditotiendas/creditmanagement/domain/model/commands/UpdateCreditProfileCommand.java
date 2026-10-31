package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;

import java.math.BigDecimal;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Changes the agreed conditions of a credit profile. Sales already registered keep their rate snapshot (US-04).
 */
public record UpdateCreditProfileCommand(Long creditProfileId, InterestRate interestRate,
                                         LateInterestRate lateInterestRate, Long currencyId, BigDecimal creditLimit,
                                         Integer maxTermDays, Integer cutoffDay, Integer paymentDay) {
    public UpdateCreditProfileCommand {
        creditProfileId = requireId(creditProfileId, "creditProfileId");
        requireNonNull(interestRate, "interestRate");
        requireNonNull(lateInterestRate, "lateInterestRate");
        currencyId = requireId(currencyId, "currencyId");
        creditLimit = requirePositiveAmount(creditLimit, "creditLimit");
        maxTermDays = requireRange(maxTermDays, "maxTermDays", 1, MAX_TERM_DAYS);
        cutoffDay = requireRange(cutoffDay, "cutoffDay", 1, 28);
        paymentDay = requireRange(paymentDay, "paymentDay", 1, 28);
    }
}
