package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.filter.TransferenceFilter;
import br.com.webbudget.domain.entities.financial.Transference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransferenceHistoricViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void filter_fields_are_present() {
        var presenter = mock(TransferenceHistoricPresenter.class);
        when(presenter.loadWallets()).thenReturn(List.of());
        when(presenter.filter(any(TransferenceFilter.class))).thenReturn(List.of());

        var view = new TransferenceHistoricView(presenter);

        assertThat(view.originFilter).isNotNull();
        assertThat(view.destinationFilter).isNotNull();
        assertThat(view.dateFilter).isNotNull();
        assertThat(view.originFilter.isClearButtonVisible()).isTrue();
        assertThat(view.destinationFilter.isClearButtonVisible()).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_5_columns() {
        var presenter = mock(TransferenceHistoricPresenter.class);
        when(presenter.loadWallets()).thenReturn(List.of());
        when(presenter.filter(any(TransferenceFilter.class))).thenReturn(List.of());

        var view = new TransferenceHistoricView(presenter);

        assertThat(view.grid.getColumns()).hasSize(5);
    }
}
