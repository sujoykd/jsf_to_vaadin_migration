package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.registration.Wallet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormTransferenceViewTest {

    @Test
    void form_has_wallet_comboboxes() {
        var presenter = mock(FormTransferencePresenter.class);
        when(presenter.loadWallets()).thenReturn(List.of());

        var view = new FormTransferenceView(presenter);

        assertThat(view.originComboBox).isNotNull();
        assertThat(view.originComboBox.getLabel()).isEqualTo("Origin Wallet");
        assertThat(view.destinationComboBox).isNotNull();
        assertThat(view.destinationComboBox.getLabel()).isEqualTo("Destination Wallet");
    }

    @Test
    void transfer_button_is_present() {
        var presenter = mock(FormTransferencePresenter.class);
        when(presenter.loadWallets()).thenReturn(List.of());

        var view = new FormTransferenceView(presenter);

        assertThat(view.valueField).isNotNull();
        assertThat(view.transferDatePicker).isNotNull();

        long buttonCount = view.getChildren()
                .flatMap(c -> {
                    if (c instanceof com.vaadin.flow.component.orderedlayout.HorizontalLayout hl) {
                        return hl.getChildren();
                    }
                    return java.util.stream.Stream.empty();
                })
                .filter(c -> c instanceof com.vaadin.flow.component.button.Button)
                .count();

        assertThat(buttonCount).isGreaterThanOrEqualTo(1);
    }
}
