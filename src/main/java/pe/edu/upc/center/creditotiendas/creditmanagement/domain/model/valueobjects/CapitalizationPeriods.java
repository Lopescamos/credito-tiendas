package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

/**
 * Capitalization period of a nominal rate, in days of a 360-day year (30-day months).
 */
public enum CapitalizationPeriods {
    DAILY(1),
    BIWEEKLY(15),
    MONTHLY(30),
    BIMONTHLY(60),
    QUARTERLY(90),
    FOUR_MONTHLY(120),
    SEMIANNUAL(180),
    ANNUAL(360);

    private final int days;

    CapitalizationPeriods(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
