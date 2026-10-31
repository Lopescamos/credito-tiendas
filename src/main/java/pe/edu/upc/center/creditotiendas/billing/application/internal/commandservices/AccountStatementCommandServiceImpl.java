package pe.edu.upc.center.creditotiendas.billing.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.AccountStatementCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.internal.outboundservices.acl.ExternalCreditService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.GenerateAccountStatementCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.AccountStatementDetail;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.MovementTypes;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.AccountStatementRepository;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.LateFeeRepository;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.PaymentRepository;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * Generates a monthly account statement at the profile's cut-off day (US-06). A statement for a profile and
 * period is created only once; firing the command again returns the existing one.
 *
 * <p>Formula: {@code finalBalance = previousBalance + periodPurchases + periodInterest + periodLateFees - periodPayments}.
 * The previous balance is the {@code finalBalance} of the newest earlier statement (0 if none).</p>
 */
@Slf4j
@Service
public class AccountStatementCommandServiceImpl implements AccountStatementCommandService {

    /** Stores run in Lima; the cut-off date is a civil date in that zone. */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    private final AccountStatementRepository accountStatementRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final LateFeeRepository lateFeeRepository;
    private final ExternalCreditService externalCreditService;

    public AccountStatementCommandServiceImpl(AccountStatementRepository accountStatementRepository,
                                              InstallmentRepository installmentRepository,
                                              PaymentRepository paymentRepository,
                                              LateFeeRepository lateFeeRepository,
                                              ExternalCreditService externalCreditService) {
        this.accountStatementRepository = accountStatementRepository;
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.lateFeeRepository = lateFeeRepository;
        this.externalCreditService = externalCreditService;
    }

    @Override
    public Result<AccountStatement, ApplicationError> handle(GenerateAccountStatementCommand command) {
        var profile = externalCreditService.fetchProfile(command.creditProfileId());
        if (profile.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditProfile", command.creditProfileId().toString()));
        if (accountStatementRepository.existsByCreditProfileIdAndPeriod(command.creditProfileId(), command.period()))
            return Result.failure(ApplicationError.conflict("AccountStatement",
                    "Statement for profile %d period %s already exists".formatted(
                            command.creditProfileId(), command.period())));

        var cutoffDate = command.period().atDay(Math.min(profile.get().cutoffDay(), command.period().lengthOfMonth()));
        var previousStatement = accountStatementRepository.findLatestByCreditProfileIdBefore(
                command.creditProfileId(), command.period());
        var previousBalance = previousStatement.map(AccountStatement::getFinalBalance).orElse(BigDecimal.ZERO);
        var periodStart = previousStatement.map(AccountStatement::getCutoffDate).map(d -> d.plusDays(1))
                .orElse(command.period().atDay(1));

        var periodInstallments = installmentRepository.findAllByCreditProfileIdInRange(
                command.creditProfileId(), periodStart, cutoffDate);
        // Billing does not have a direct "purchases" line, so the period's capital and interest are approximated
        // as the amounts of the installments that fall due in the range (both paid and unpaid).
        var periodInterest = periodInstallments.stream().map(i -> i.getInterest()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var periodPurchases = periodInstallments.stream().map(i -> i.getCapital()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var periodPayments = paymentRepository.sumAmountByCreditProfileIdInRange(
                command.creditProfileId(), periodStart, cutoffDate);
        var periodLateFees = lateFeeRepository.sumLatestByCreditProfileIdInRange(
                command.creditProfileId(),
                periodStart.atStartOfDay(BUSINESS_ZONE).toInstant(),
                cutoffDate.atTime(LocalTime.MAX).atZone(BUSINESS_ZONE).toInstant());
        var finalBalance = previousBalance.add(periodPurchases).add(periodInterest).add(periodLateFees).subtract(periodPayments);

        var details = new ArrayList<AccountStatementDetail>();
        if (periodPurchases.signum() > 0)
            details.add(new AccountStatementDetail(MovementTypes.PURCHASE, "Purchases of the period", periodPurchases, cutoffDate));
        if (periodInterest.signum() > 0)
            details.add(new AccountStatementDetail(MovementTypes.INTEREST, "Compensatory interest", periodInterest, cutoffDate));
        if (periodLateFees.signum() > 0)
            details.add(new AccountStatementDetail(MovementTypes.LATE_FEE, "Late fees", periodLateFees, cutoffDate));
        if (periodPayments.signum() > 0)
            details.add(new AccountStatementDetail(MovementTypes.PAYMENT, "Payments received", periodPayments, cutoffDate));

        try {
            var statement = new AccountStatement(command.creditProfileId(), command.period(), cutoffDate,
                    previousBalance, periodPurchases, periodPayments, periodInterest, periodLateFees, finalBalance, details);
            var saved = accountStatementRepository.save(statement);
            log.info("Generated account statement {} for profile {} period {}", saved.getId(),
                    command.creditProfileId(), command.period());
            return Result.success(saved);
        } catch (Exception e) {
            log.error("Error in generate-account-statement", e);
            return Result.failure(ApplicationError.unexpected("generate-account-statement",
                    "The account statement could not be generated"));
        }
    }
}
