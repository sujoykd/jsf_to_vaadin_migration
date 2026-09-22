package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.journal.Refueling;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListRefuelingsViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_7_columns() {
        var presenter = mock(ListRefuelingsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListRefuelingsView(presenter);

        assertThat(view.grid.getColumns()).hasSize(7);
    }

    @SuppressWarnings("unchecked")
    @Test
    void filter_field_is_present() {
        var presenter = mock(ListRefuelingsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListRefuelingsView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
    }
}
