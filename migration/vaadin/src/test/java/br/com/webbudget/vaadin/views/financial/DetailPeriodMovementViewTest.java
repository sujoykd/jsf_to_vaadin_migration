package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DetailPeriodMovementViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailPeriodMovementPresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());

        var view = new DetailPeriodMovementView(presenter);

        assertThat(view.identificationField).isNotNull();
        assertThat(view.financialPeriodField).isNotNull();
        assertThat(view.dueDateField).isNotNull();
        assertThat(view.amountField).isNotNull();
    }

    @Test
    void all_fields_are_read_only() {
        var presenter = mock(DetailPeriodMovementPresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());

        var view = new DetailPeriodMovementView(presenter);

        assertThat(view.identificationField.isReadOnly()).isTrue();
    }
}
