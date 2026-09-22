package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailFinancialPeriodViewTest {
    @Test
    void fields_are_present() {
        var view = new DetailFinancialPeriodView(mock(DetailFinancialPeriodPresenter.class));
        assertThat(view.identificationField).isNotNull();
        assertThat(view.startField).isNotNull();
        assertThat(view.endField).isNotNull();
        assertThat(view.closedCheckbox).isNotNull();
    }
    @Test
    void fields_are_read_only() {
        var view = new DetailFinancialPeriodView(mock(DetailFinancialPeriodPresenter.class));
        assertThat(view.identificationField.isReadOnly()).isTrue();
        assertThat(view.startField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new DetailFinancialPeriodView(mock(DetailFinancialPeriodPresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.reopenButton).isNotNull();
        assertThat(view.statisticsButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
