package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.repositories.financial.CreditCardInvoiceRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardInvoicePrintViewTest {

    @Test
    void view_has_print_button() {
        var repository = mock(CreditCardInvoiceRepository.class);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        var view = new CardInvoicePrintView(repository);

        assertThat(view.printButton).isNotNull();
        assertThat(view.printButton.getText()).isEqualTo("Print");
    }

    @Test
    void view_has_movements_grid() {
        var repository = mock(CreditCardInvoiceRepository.class);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        var view = new CardInvoicePrintView(repository);

        assertThat(view.movementsGrid).isNotNull();
        assertThat(view.movementsGrid.getColumns()).hasSize(2);
    }

    @Test
    void view_has_invoice_info_spans() {
        var repository = mock(CreditCardInvoiceRepository.class);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        var view = new CardInvoicePrintView(repository);

        assertThat(view.cardNameSpan).isNotNull();
        assertThat(view.identificationSpan).isNotNull();
        assertThat(view.totalSpan).isNotNull();
    }
}
