package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BalanceAdjustmentViewTest {
    @Test
    void fields_are_present() {
        var view = new BalanceAdjustmentView(mock(BalanceAdjustmentPresenter.class));
        assertThat(view.currentBalanceField).isNotNull();
        assertThat(view.adjustmentField).isNotNull();
        assertThat(view.reasonArea).isNotNull();
    }
    @Test
    void current_balance_is_read_only() {
        var view = new BalanceAdjustmentView(mock(BalanceAdjustmentPresenter.class));
        assertThat(view.currentBalanceField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new BalanceAdjustmentView(mock(BalanceAdjustmentPresenter.class));
        assertThat(view.saveButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
