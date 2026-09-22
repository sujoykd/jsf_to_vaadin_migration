package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetailCreditCardInvoiceViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailCreditCardInvoicePresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());

        var view = new DetailCreditCardInvoiceView(presenter);

        assertThat(view.identificationField).isNotNull();
        assertThat(view.cardField).isNotNull();
        assertThat(view.movementsGrid).isNotNull();
    }

    @Test
    void movements_grid_has_expected_columns() {
        var presenter = mock(DetailCreditCardInvoicePresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());

        var view = new DetailCreditCardInvoiceView(presenter);

        assertThat(view.movementsGrid.getColumns()).hasSizeGreaterThanOrEqualTo(3);
    }
}
