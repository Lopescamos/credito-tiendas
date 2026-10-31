package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;

import java.math.BigDecimal;
import java.util.Locale;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Opens a credit account for a customer in a store with the agreed conditions (US-04).
 * The customer is identified by the email of their CLIENT user.
 *
 * @param cutoffDay  day of the month when the account statement is closed (1-28)
 * @param paymentDay day of the month when the statement is due (1-28)
 */
public record CreateCreditProfileCommand(Long storeId, String clientEmail, InterestRate interestRate,
                                         LateInterestRate lateInterestRate, Long currencyId, BigDecimal creditLimit,
                                         Integer maxTermDays, Integer cutoffDay, Integer paymentDay) {
    public CreateCreditProfileCommand {
        storeId = requireId(storeId, "storeId");
        clientEmail = requireText(clientEmail, "clientEmail", 100).toLowerCase(Locale.ROOT);
        requireNonNull(interestRate, "interestRate");
        requireNonNull(lateInterestRate, "lateInterestRate");
        currencyId = requireId(currencyId, "currencyId");
        creditLimit = requirePositiveAmount(creditLimit, "creditLimit");
        maxTermDays = requireRange(maxTermDays, "maxTermDays", 1, MAX_TERM_DAYS);
        cutoffDay = requireRange(cutoffDay, "cutoffDay", 1, 28);
        paymentDay = requireRange(paymentDay, "paymentDay", 1, 28);
    }
}
