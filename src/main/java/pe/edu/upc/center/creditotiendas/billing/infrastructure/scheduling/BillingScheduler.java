package pe.edu.upc.center.creditotiendas.billing.infrastructure.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.AccountStatementCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.InstallmentCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.internal.outboundservices.acl.ExternalCreditService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.GenerateAccountStatementCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.MarkOverdueInstallmentsCommand;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Daily jobs that keep Billing in sync with real time:
 * <ul>
 *   <li>Overdue sweep: flips every installment whose due date passed to {@code OVERDUE} and moves the owning
 *       sale to {@code OVERDUE} in Credit.</li>
 *   <li>Account statements: for every active profile whose cut-off day is today, generates its monthly
 *       statement for the current period (US-06). Already-generated statements are skipped.</li>
 * </ul>
 *
 * <p>Both run at 00:05 Lima time, after midnight, so a payment registered on the due date never ends up as
 * overdue by mistake.</p>
 */
@Slf4j
@Component
public class BillingScheduler {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    private final InstallmentCommandService installmentCommandService;
    private final AccountStatementCommandService accountStatementCommandService;
    private final ExternalCreditService externalCreditService;

    public BillingScheduler(InstallmentCommandService installmentCommandService,
                            AccountStatementCommandService accountStatementCommandService,
                            ExternalCreditService externalCreditService) {
        this.installmentCommandService = installmentCommandService;
        this.accountStatementCommandService = accountStatementCommandService;
        this.externalCreditService = externalCreditService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    public void sweepOverdueInstallments() {
        var today = LocalDate.now(BUSINESS_ZONE);
        try {
            installmentCommandService.handle(new MarkOverdueInstallmentsCommand(today));
        } catch (Exception e) {
            log.error("Overdue sweep failed for {}", today, e);
        }
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "America/Lima")
    public void generateDueStatements() {
        var today = LocalDate.now(BUSINESS_ZONE);
        var cutoffDay = today.getDayOfMonth();
        var period = YearMonth.from(today);
        for (var profileId : externalCreditService.fetchActiveProfileIdsByCutoffDay(cutoffDay)) {
            try {
                accountStatementCommandService.handle(new GenerateAccountStatementCommand(profileId, period));
            } catch (Exception e) {
                log.error("Account statement failed for profile {} period {}", profileId, period, e);
            }
        }
    }
}
