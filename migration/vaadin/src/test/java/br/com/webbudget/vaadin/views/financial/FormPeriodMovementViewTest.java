package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormPeriodMovementViewTest {

    private FormPeriodMovementView createView() {
        var presenter = mock(FormPeriodMovementPresenter.class);
        when(presenter.findOpenPeriods()).thenReturn(List.of());
        when(presenter.findActiveContacts()).thenReturn(List.of());
        return new FormPeriodMovementView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.identificationField).isNotNull();
        assertThat(view.financialPeriodComboBox).isNotNull();
        assertThat(view.dueDatePicker).isNotNull();
        assertThat(view.amountField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = createView();
        assertThat(view.identificationField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.saveButton).isNotNull();
        assertThat(view.saveAndPayButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
