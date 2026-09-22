package br.com.webbudget.application.components.ui.filter;

import br.com.webbudget.domain.entities.financial.PeriodMovementState;
import br.com.webbudget.domain.entities.financial.PeriodMovementType;
import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.MovementClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class PeriodMovementFilter extends BasicFilter {

    @Setter
    @Getter
    private CostCenter costCenter;
    @Setter
    @Getter
    private MovementClass movementClass;
    @Setter
    @Getter
    private PeriodMovementType periodMovementType;
    @Setter
    @Getter
    private PeriodMovementState periodMovementState;

    @Setter
    private List<FinancialPeriod> selectedFinancialPeriods;

    public PeriodMovementFilter() {
        this.selectedFinancialPeriods = new ArrayList<>();
    }

    public void clear() {
        this.value = null;
        this.costCenter = null;
        this.movementClass = null;
        this.periodMovementType = null;
        this.periodMovementState = null;
        this.selectedFinancialPeriods = new ArrayList<>();
    }

    public Optional<BigDecimal> valueToBigDecimal() {
        try {
            return Optional.of(new BigDecimal(this.value.replace(",", ".")));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public List<FinancialPeriod> getSelectedFinancialPeriods() {
        return this.selectedFinancialPeriods == null ? List.of() : this.selectedFinancialPeriods;
    }

    public PeriodMovementType[] getMovementTypes() {
        return PeriodMovementType.values();
    }

    public PeriodMovementState[] getPeriodMovementStates() {
        return PeriodMovementState.values();
    }

    public String[] getSelectedFinancialPeriodsAsStringArray() {
        return this.selectedFinancialPeriods.stream()
                .map(FinancialPeriod::getIdentification)
                .collect(Collectors.toList())
                .toArray(String[]::new);
    }
}
