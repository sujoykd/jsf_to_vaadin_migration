package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormMovementClassViewTest {

    private FormMovementClassView createView() {
        var presenter = mock(FormMovementClassPresenter.class);
        when(presenter.findActiveCostCenters()).thenReturn(List.of());
        return new FormMovementClassView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.nameField).isNotNull();
        assertThat(view.movementClassTypeSelect).isNotNull();
        assertThat(view.costCenterComboBox).isNotNull();
        assertThat(view.budgetField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = createView();
        assertThat(view.nameField.isRequiredIndicatorVisible()).isTrue();
        assertThat(view.budgetField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
