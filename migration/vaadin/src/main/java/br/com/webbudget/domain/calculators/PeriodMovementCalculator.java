package br.com.webbudget.domain.calculators;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.financial.PeriodMovementRepository;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PeriodMovementCalculator {

    private List<PeriodMovement> expenses = new ArrayList<>();
    private List<PeriodMovement> revenues = new ArrayList<>();
    private List<PeriodMovement> movements = new ArrayList<>();

    private final PeriodMovementRepository periodMovementRepository;
    private final FinancialPeriodRepository financialPeriodRepository;

    @Transactional(readOnly = true)
    public void load(FinancialPeriod financialPeriod) {
        this.movements = this.periodMovementRepository.findByFinancialPeriod(financialPeriod);
        this.splitByType();
    }

    @Transactional(readOnly = true)
    public void load() {
        this.movements = new ArrayList<>();
        final List<FinancialPeriod> openPeriods = this.financialPeriodRepository.findByClosedOrderByIdentificationAsc(false);
        openPeriods.forEach(period -> this.movements.addAll(this.periodMovementRepository.findByFinancialPeriod(period)));
        this.splitByType();
    }

    private void splitByType() {
        this.expenses = this.movements.stream()
                .filter(PeriodMovement::isExpense)
                .collect(Collectors.toList());
        this.revenues = this.movements.stream()
                .filter(PeriodMovement::isRevenue)
                .collect(Collectors.toList());
    }

    public BigDecimal getCreditCardExpensesValue() {
        return this.expenses.stream()
                .filter(PeriodMovement::isPaidWithCreditCard)
                .map(PeriodMovement::getValueWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDebitCardExpensesValue() {
        return this.expenses.stream()
                .filter(PeriodMovement::isPaidWithDebitCard)
                .map(PeriodMovement::getValueWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCashExpensesValue() {
        return this.expenses.stream()
                .filter(PeriodMovement::isPaidWithCash)
                .map(PeriodMovement::getValueWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRevenuesValue() {
        return this.revenues.stream()
                .map(PeriodMovement::getValueWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getExpensesValue() {
        return this.expenses.stream()
                .filter(movement -> !movement.isPaidWithCreditCard())
                .map(PeriodMovement::getValueWithDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
