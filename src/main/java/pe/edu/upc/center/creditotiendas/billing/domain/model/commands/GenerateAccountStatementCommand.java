package pe.edu.upc.center.creditotiendas.billing.domain.model.commands;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Generates the monthly account statement of a profile for a given period (US-06). The scheduler fires it on
 * each profile's cutoff day; already-generated statements are not re-created.
 */
public record GenerateAccountStatementCommand(Long creditProfileId, YearMonth period) {
    public GenerateAccountStatementCommand {
        if (Objects.isNull(creditProfileId) || creditProfileId <= 0)
            throw new IllegalArgumentException("[creditProfileId] must be a positive number");
        if (Objects.isNull(period))
            throw new IllegalArgumentException("[period] cannot be null");
    }
}
