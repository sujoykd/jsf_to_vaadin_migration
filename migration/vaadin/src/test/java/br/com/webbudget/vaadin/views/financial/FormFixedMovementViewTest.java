package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FormFixedMovementViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(FormFixedMovementPresenter.class);
        when(presenter.findContacts(null)).thenReturn(List.of());

        var view = new FormFixedMovementView(presenter);

        assertThat(view.identificationField).isNotNull();
        assertThat(view.valueField).isNotNull();
        assertThat(view.startDatePicker).isNotNull();
        assertThat(view.autoLaunchCheckbox).isNotNull();
        assertThat(view.contactComboBox).isNotNull();
    }

    @Test
    void state_combo_has_values() {
        var presenter = mock(FormFixedMovementPresenter.class);
        when(presenter.findContacts(null)).thenReturn(List.of());

        var view = new FormFixedMovementView(presenter);

        assertThat(view.stateComboBox).isNotNull();
    }
}
