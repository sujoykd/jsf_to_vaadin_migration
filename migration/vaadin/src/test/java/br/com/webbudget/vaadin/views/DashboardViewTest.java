package br.com.webbudget.vaadin.views;

import br.com.webbudget.domain.calculators.PeriodMovementCalculator;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DashboardViewTest {

    @Test
    void view_renders_without_errors() {
        var financialPeriodRepository = mock(FinancialPeriodRepository.class);
        var walletRepository = mock(WalletRepository.class);
        var calculator = mock(PeriodMovementCalculator.class);

        when(financialPeriodRepository.findByClosedOrderByIdentificationAsc(false)).thenReturn(List.of());
        when(walletRepository.count()).thenReturn(0L);
        when(calculator.getExpensesValue()).thenReturn(BigDecimal.ZERO);
        when(calculator.getRevenuesValue()).thenReturn(BigDecimal.ZERO);
        when(calculator.getCreditCardExpensesValue()).thenReturn(BigDecimal.ZERO);

        var view = new DashboardView(financialPeriodRepository, walletRepository, calculator);

        assertThat(view).isNotNull();
    }

    @Test
    void view_shows_open_periods_count() {
        var financialPeriodRepository = mock(FinancialPeriodRepository.class);
        var walletRepository = mock(WalletRepository.class);
        var calculator = mock(PeriodMovementCalculator.class);

        when(financialPeriodRepository.findByClosedOrderByIdentificationAsc(false)).thenReturn(List.of());
        when(walletRepository.count()).thenReturn(3L);
        when(calculator.getExpensesValue()).thenReturn(BigDecimal.ZERO);
        when(calculator.getRevenuesValue()).thenReturn(BigDecimal.ZERO);
        when(calculator.getCreditCardExpensesValue()).thenReturn(BigDecimal.ZERO);

        var view = new DashboardView(financialPeriodRepository, walletRepository, calculator);

        assertThat(view.openPeriodsCount).isNotNull();
        assertThat(view.walletsCount.getText()).isEqualTo("3");
    }

    @Test
    void view_displays_expense_and_revenue_spans() {
        var financialPeriodRepository = mock(FinancialPeriodRepository.class);
        var walletRepository = mock(WalletRepository.class);
        var calculator = mock(PeriodMovementCalculator.class);

        when(financialPeriodRepository.findByClosedOrderByIdentificationAsc(false)).thenReturn(List.of());
        when(walletRepository.count()).thenReturn(0L);
        when(calculator.getExpensesValue()).thenReturn(new BigDecimal("100.00"));
        when(calculator.getRevenuesValue()).thenReturn(new BigDecimal("200.00"));
        when(calculator.getCreditCardExpensesValue()).thenReturn(new BigDecimal("50.00"));

        var view = new DashboardView(financialPeriodRepository, walletRepository, calculator);

        assertThat(view.expensesValue).isNotNull();
        assertThat(view.revenuesValue).isNotNull();
        assertThat(view.creditCardExpenses).isNotNull();
    }
}
