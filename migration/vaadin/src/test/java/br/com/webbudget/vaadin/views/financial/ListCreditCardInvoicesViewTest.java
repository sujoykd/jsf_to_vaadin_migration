package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListCreditCardInvoicesViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_5_columns() {
        var presenter = mock(ListCreditCardInvoicesPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListCreditCardInvoicesView(presenter);

        assertThat(view.grid.getColumns()).hasSize(6);
    }

    @SuppressWarnings("unchecked")
    @Test
    void filter_fields_are_present() {
        var presenter = mock(ListCreditCardInvoicesPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListCreditCardInvoicesView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
        assertThat(view.stateFilter).isNotNull();
    }
}
