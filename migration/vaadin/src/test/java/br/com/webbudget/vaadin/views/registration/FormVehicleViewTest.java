package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormVehicleViewTest {

    private FormVehicleView createView() {
        var presenter = mock(FormVehiclePresenter.class);
        when(presenter.findActiveCostCenters()).thenReturn(List.of());
        return new FormVehicleView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.identificationField).isNotNull();
        assertThat(view.brandField).isNotNull();
        assertThat(view.vehicleTypeSelect).isNotNull();
        assertThat(view.costCenterComboBox).isNotNull();
        assertThat(view.licensePlateField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = createView();
        assertThat(view.identificationField.isRequiredIndicatorVisible()).isTrue();
        assertThat(view.brandField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
