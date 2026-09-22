package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FormFinancialPeriodViewTest {

    @Test
    void fields_are_present() {
        var view = new FormFinancialPeriodView(mock(FormFinancialPeriodPresenter.class));
        assertThat(view.identificationField).isNotNull();
        assertThat(view.startDatePicker).isNotNull();
        assertThat(view.endDatePicker).isNotNull();
        assertThat(view.creditCardGoalField).isNotNull();
        assertThat(view.expensesGoalField).isNotNull();
        assertThat(view.incomesGoalField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = new FormFinancialPeriodView(mock(FormFinancialPeriodPresenter.class));
        assertThat(view.identificationField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void save_button_is_present() {
        var view = new FormFinancialPeriodView(mock(FormFinancialPeriodPresenter.class));
        assertThat(view.saveButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
