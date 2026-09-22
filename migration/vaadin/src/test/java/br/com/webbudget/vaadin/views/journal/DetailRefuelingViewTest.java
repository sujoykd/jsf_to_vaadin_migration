package br.com.webbudget.vaadin.views.journal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailRefuelingViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailRefuelingPresenter.class);
        var view = new DetailRefuelingView(presenter);

        assertThat(view.fullTankCheckbox).isNotNull();
        assertThat(view.enterFinancialCheckbox).isNotNull();
        assertThat(view.refuelingDateField).isNotNull();
        assertThat(view.odometerField).isNotNull();
        assertThat(view.vehicleField).isNotNull();
        assertThat(view.locationField).isNotNull();
        assertThat(view.movementClassField).isNotNull();
        assertThat(view.financialPeriodField).isNotNull();
    }

    @Test
    void fuels_grid_has_three_columns() {
        var presenter = mock(DetailRefuelingPresenter.class);
        var view = new DetailRefuelingView(presenter);

        assertThat(view.fuelsGrid.getColumns()).hasSize(3);
    }

    @Test
    void action_buttons_are_present() {
        var presenter = mock(DetailRefuelingPresenter.class);
        var view = new DetailRefuelingView(presenter);

        assertThat(view.deleteButton).isNotNull();
        assertThat(view.createMovementButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }

    @Test
    void fields_are_read_only() {
        var presenter = mock(DetailRefuelingPresenter.class);
        var view = new DetailRefuelingView(presenter);

        assertThat(view.refuelingDateField.isReadOnly()).isTrue();
        assertThat(view.odometerField.isReadOnly()).isTrue();
        assertThat(view.vehicleField.isReadOnly()).isTrue();
        assertThat(view.movementClassField.isReadOnly()).isTrue();
        assertThat(view.financialPeriodField.isReadOnly()).isTrue();
    }
}
