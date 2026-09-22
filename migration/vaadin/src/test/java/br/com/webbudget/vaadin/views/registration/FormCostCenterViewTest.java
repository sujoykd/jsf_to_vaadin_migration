package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormCostCenterViewTest {

    private FormCostCenterView createView() {
        var presenter = mock(FormCostCenterPresenter.class);
        when(presenter.findAllActive()).thenReturn(List.of());
        return new FormCostCenterView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.nameField).isNotNull();
        assertThat(view.incomeBudgetField).isNotNull();
        assertThat(view.expenseBudgetField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = createView();
        assertThat(view.nameField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
