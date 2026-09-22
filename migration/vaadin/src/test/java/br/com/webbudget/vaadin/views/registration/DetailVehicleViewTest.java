package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailVehicleViewTest {
    @Test
    void fields_are_present() {
        var view = new DetailVehicleView(mock(DetailVehiclePresenter.class));
        assertThat(view.identificationField).isNotNull();
        assertThat(view.brandField).isNotNull();
        assertThat(view.modelField).isNotNull();
        assertThat(view.licensePlateField).isNotNull();
    }
    @Test
    void fields_are_read_only() {
        var view = new DetailVehicleView(mock(DetailVehiclePresenter.class));
        assertThat(view.identificationField.isReadOnly()).isTrue();
        assertThat(view.brandField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new DetailVehicleView(mock(DetailVehiclePresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
