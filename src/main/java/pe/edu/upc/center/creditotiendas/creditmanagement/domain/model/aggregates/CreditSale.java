package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.SaleDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.PaymentPlanCalculator;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sale on credit to a customer (model: Venta_Credito + Detalle_Venta, US-02).
 *
 * <p>At registration it copies the 5 rate columns of the profile (rate, rate type, capitalization period,
 * late rate, late rate type) into its own snapshot. Every later calculation of this sale uses only that
 * snapshot, so editing the profile never changes past sales (professor's correction, US-04, US-07).</p>
 */
@Getter
public class CreditSale extends AbstractDomainAggregateRoot<CreditSale> {

    /** Stores operate in Peru: "today" for a sale is the date in Lima, whatever the server time zone. */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    @Setter private Long id;
    @Setter private Long creditProfileId;
    @Setter private LocalDate saleDate;
    @Setter private BigDecimal totalAmount;
    @Setter private SaleModalities modality;
    @Setter private int termDays;
    @Setter private int graceDays;
    @Setter private int installmentCount;
    @Setter private BigDecimal fixedInstallment;
    @Setter private BigDecimal financedCapital;
    @Setter private InterestRate interestRateSnapshot;
    @Setter private LateInterestRate lateInterestRateSnapshot;
    @Setter private CreditSaleStatuses status;
    @Setter private Long originPurchaseRequestId;
    private final List<SaleDetail> details = new ArrayList<>();

    public CreditSale() {
    }

    /**
     * Builds a sale with the current conditions of the profile. Credit limit, term and catalog rules are
     * checked by the application service before calling this.
     *
     * @param originPurchaseRequestId the approved request that generated the sale, or {@code null}
     */
    public static CreditSale register(CreditProfile profile, LocalDate saleDate, SaleModalities modality,
                                      int installmentCount, int graceDays, List<SaleDetail> details,
                                      Long originPurchaseRequestId) {
        if (details.isEmpty())
            throw new IllegalArgumentException("[CreditSale] a sale needs at least one detail");

        var sale = new CreditSale();
        sale.creditProfileId = profile.getId();
        sale.saleDate = saleDate;
        sale.modality = modality;
        sale.installmentCount = installmentCount;
        sale.graceDays = graceDays;
        sale.termDays = termDaysOf(graceDays, installmentCount);
        sale.details.addAll(details);
        sale.totalAmount = details.stream().map(SaleDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        sale.interestRateSnapshot = profile.getInterestRate();
        sale.lateInterestRateSnapshot = profile.getLateInterestRate();
        sale.status = CreditSaleStatuses.ACTIVE;
        sale.originPurchaseRequestId = originPurchaseRequestId;

        var plan = sale.paymentPlan();
        sale.financedCapital = plan.financedCapital();
        sale.fixedInstallment = plan.fixedInstallment();
        return sale;
    }

    /**
     * Total days from the sale to the last installment: grace days plus one 30-day period per installment.
     */
    public static int termDaysOf(int graceDays, int installmentCount) {
        return graceDays + PaymentPlanCalculator.PERIOD_DAYS * installmentCount;
    }

    /**
     * Recomputes the payment plan from the rate snapshot. It is deterministic, so it always matches the
     * plan shown when the sale was registered.
     */
    public PaymentPlan paymentPlan() {
        return PaymentPlanCalculator.calculate(totalAmount, interestRateSnapshot, graceDays, installmentCount, saleDate);
    }

    public List<SaleDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    /**
     * Replaces the details. Used by the persistence assembler to rebuild the aggregate.
     */
    public void setDetails(List<SaleDetail> details) {
        this.details.clear();
        this.details.addAll(details);
    }
}
