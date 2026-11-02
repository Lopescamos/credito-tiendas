package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * The credit account of one customer in one store, with the conditions agreed in person (US-04):
 * rates, currency, credit limit, maximum term and cut-off/payment days. A customer has at most one
 * profile per store. Changing the conditions never alters sales already registered: each sale keeps
 * a snapshot of the rates.
 */
@Getter
public class CreditProfile extends AbstractDomainAggregateRoot<CreditProfile> {

    @Setter private Long id;
    @Setter private Long userId;
    @Setter private Long storeId;
    @Setter private InterestRate interestRate;
    @Setter private LateInterestRate lateInterestRate;
    @Setter private Long currencyId;
    @Setter private BigDecimal creditLimit;
    @Setter private int maxTermDays;
    @Setter private int cutoffDay;
    @Setter private int paymentDay;
    @Setter private boolean active;

    public CreditProfile() {
        this.active = true;
    }

    public CreditProfile(CreateCreditProfileCommand command, Long userId) {
        this();
        this.userId = userId;
        this.storeId = command.storeId();
        this.interestRate = command.interestRate();
        this.lateInterestRate = command.lateInterestRate();
        this.currencyId = command.currencyId();
        this.creditLimit = command.creditLimit();
        this.maxTermDays = command.maxTermDays();
        this.cutoffDay = command.cutoffDay();
        this.paymentDay = command.paymentDay();
    }

    public CreditProfile updateConditions(InterestRate interestRate, LateInterestRate lateInterestRate, Long currencyId,
                                          BigDecimal creditLimit, int maxTermDays, int cutoffDay, int paymentDay) {
        this.interestRate = interestRate;
        this.lateInterestRate = lateInterestRate;
        this.currencyId = currencyId;
        this.creditLimit = creditLimit;
        this.maxTermDays = maxTermDays;
        this.cutoffDay = cutoffDay;
        this.paymentDay = paymentDay;
        return this;
    }

    public CreditProfile activate() {
        this.active = true;
        return this;
    }

    public CreditProfile deactivate() {
        this.active = false;
        return this;
    }

    /**
     * US-02 rules for a new credit sale on this profile: the profile is active, the amount fits in the
     * available credit and the term does not exceed the agreed maximum.
     *
     * @param pendingCapital capital still owed on this profile's unpaid installments (Billing)
     * @param reservedCredit amount committed by this profile's other pending purchase requests
     * @return why the sale is not allowed, or empty if it is
     */
    public Optional<String> saleRejectionReason(BigDecimal amount, int termDays, BigDecimal pendingCapital,
                                                BigDecimal reservedCredit) {
        if (!active)
            return Optional.of("Credit profile %d is deactivated and cannot buy on credit".formatted(id));
        var available = availableCredit(pendingCapital, reservedCredit);
        if (amount.compareTo(available) > 0)
            return Optional.of("Amount %s exceeds the available credit %s of credit profile %d".formatted(amount, available, id));
        if (termDays > maxTermDays)
            return Optional.of("Term of %d days exceeds the maximum of %d days of credit profile %d".formatted(termDays, maxTermDays, id));
        return Optional.empty();
    }

    /**
     * Credit limit minus the capital still financed (unpaid installments in Billing) and the amount
     * reserved by this profile's other pending purchase requests. Never negative: lowering the limit below
     * the current debt leaves no available credit instead of a negative one.
     *
     * @param pendingCapital capital still owed on this profile's unpaid installments (Billing)
     * @param reservedCredit amount committed by this profile's other pending purchase requests
     */
    public BigDecimal availableCredit(BigDecimal pendingCapital, BigDecimal reservedCredit) {
        return creditLimit.subtract(pendingCapital).subtract(reservedCredit).max(BigDecimal.ZERO);
    }
}
