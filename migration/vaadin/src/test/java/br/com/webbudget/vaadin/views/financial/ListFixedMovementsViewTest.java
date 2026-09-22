package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.FixedMovement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListFixedMovementsViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_at_least_3_columns() {
        var presenter = mock(ListFixedMovementsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListFixedMovementsView(presenter);

        assertThat(view.grid.getColumns()).hasSizeGreaterThanOrEqualTo(3);
    }

    @SuppressWarnings("unchecked")
    @Test
    void filter_field_is_present() {
        var presenter = mock(ListFixedMovementsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListFixedMovementsView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
    }
}
