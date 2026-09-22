package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPeriodMovementsViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_at_least_6_columns() {
        var presenter = mock(ListPeriodMovementsPresenter.class);
        when(presenter.findAll(any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any())).thenReturn(0);

        var view = new ListPeriodMovementsView(presenter);

        assertThat(view.grid.getColumns()).hasSizeGreaterThanOrEqualTo(6);
    }

    @SuppressWarnings("unchecked")
    @Test
    void filter_field_present() {
        var presenter = mock(ListPeriodMovementsPresenter.class);
        when(presenter.findAll(any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any())).thenReturn(0);

        var view = new ListPeriodMovementsView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
    }
}
