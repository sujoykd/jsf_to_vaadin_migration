package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FinancialPeriodStatisticsViewTest {

    @Test
    void fields_are_not_null() {
        var presenter = mock(FinancialPeriodStatisticsPresenter.class);
        var view = new FinancialPeriodStatisticsView(presenter);

        assertThat(view.identificationField).isNotNull();
        assertThat(view.startDateField).isNotNull();
        assertThat(view.endDateField).isNotNull();
    }

    @Test
    void fields_are_read_only() {
        var presenter = mock(FinancialPeriodStatisticsPresenter.class);
        var view = new FinancialPeriodStatisticsView(presenter);

        assertThat(view.identificationField.isReadOnly()).isTrue();
        assertThat(view.startDateField.isReadOnly()).isTrue();
        assertThat(view.endDateField.isReadOnly()).isTrue();
    }

    @Test
    void grids_are_present() {
        var presenter = mock(FinancialPeriodStatisticsPresenter.class);
        var view = new FinancialPeriodStatisticsView(presenter);

        assertThat(view.expensesByCostCenterGrid).isNotNull();
        assertThat(view.expensesByCostCenterGrid.getColumns()).hasSize(2);

        assertThat(view.revenuesByCostCenterGrid).isNotNull();
        assertThat(view.revenuesByCostCenterGrid.getColumns()).hasSize(2);

        assertThat(view.expensesByMovementClassGrid).isNotNull();
        assertThat(view.expensesByMovementClassGrid.getColumns()).hasSize(3);

        assertThat(view.revenuesByMovementClassGrid).isNotNull();
        assertThat(view.revenuesByMovementClassGrid.getColumns()).hasSize(3);
    }

    @Test
    void back_button_is_present() {
        var presenter = mock(FinancialPeriodStatisticsPresenter.class);
        var view = new FinancialPeriodStatisticsView(presenter);

        assertThat(view.backButton).isNotNull();
    }

    @Test
    void refresh_button_is_present() {
        var presenter = mock(FinancialPeriodStatisticsPresenter.class);
        var view = new FinancialPeriodStatisticsView(presenter);

        assertThat(view.refreshButton).isNotNull();
    }
}
