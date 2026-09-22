package br.com.webbudget.vaadin.views.journal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormRefuelingViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(FormRefuelingPresenter.class);
        when(presenter.findAllVehicles()).thenReturn(List.of());
        when(presenter.findOpenPeriods()).thenReturn(List.of());

        var view = new FormRefuelingView(presenter);

        assertThat(view.fullTankCheckbox).isNotNull();
        assertThat(view.enterFinancialCheckbox).isNotNull();
        assertThat(view.refuelingDatePicker).isNotNull();
        assertThat(view.odometerField).isNotNull();
        assertThat(view.vehicleComboBox).isNotNull();
        assertThat(view.locationField).isNotNull();
        assertThat(view.movementClassComboBox).isNotNull();
        assertThat(view.financialPeriodComboBox).isNotNull();
    }

    @Test
    void fuels_grid_is_present() {
        var presenter = mock(FormRefuelingPresenter.class);
        when(presenter.findAllVehicles()).thenReturn(List.of());
        when(presenter.findOpenPeriods()).thenReturn(List.of());

        var view = new FormRefuelingView(presenter);

        assertThat(view.fuelsGrid).isNotNull();
        assertThat(view.fuelsGrid.getColumns()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void financial_fields_always_visible() {
        var presenter = mock(FormRefuelingPresenter.class);
        when(presenter.findAllVehicles()).thenReturn(List.of());
        when(presenter.findOpenPeriods()).thenReturn(List.of());

        var view = new FormRefuelingView(presenter);

        assertThat(view.movementClassComboBox.isVisible()).isTrue();
        assertThat(view.financialPeriodComboBox.isVisible()).isTrue();
    }

    @Test
    void full_tank_checked_by_default() {
        var presenter = mock(FormRefuelingPresenter.class);
        when(presenter.findAllVehicles()).thenReturn(List.of());
        when(presenter.findOpenPeriods()).thenReturn(List.of());

        var view = new FormRefuelingView(presenter);

        assertThat(view.fullTankCheckbox.getValue()).isTrue();
    }
}
