package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormPaymentViewTest {

    private FormPaymentView createView() {
        var presenter = mock(FormPaymentPresenter.class);
        when(presenter.findActiveWallets()).thenReturn(List.of());
        when(presenter.findCreditCards()).thenReturn(List.of());
        when(presenter.findDebitCards()).thenReturn(List.of());
        return new FormPaymentView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.paidOnPicker).isNotNull();
        assertThat(view.discountField).isNotNull();
        assertThat(view.paymentMethodSelect).isNotNull();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.payButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
