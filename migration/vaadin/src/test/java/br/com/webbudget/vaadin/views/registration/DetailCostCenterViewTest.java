package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailCostCenterViewTest {
    @Test
    void fields_are_present() {
        var view = new DetailCostCenterView(mock(DetailCostCenterPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
        assertThat(view.incomeBudgetField).isNotNull();
        assertThat(view.expenseBudgetField).isNotNull();
    }
    @Test
    void fields_are_read_only() {
        var view = new DetailCostCenterView(mock(DetailCostCenterPresenter.class));
        assertThat(view.nameField.isReadOnly()).isTrue();
        assertThat(view.incomeBudgetField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new DetailCostCenterView(mock(DetailCostCenterPresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
