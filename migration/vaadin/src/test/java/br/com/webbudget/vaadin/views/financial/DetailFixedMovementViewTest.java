package br.com.webbudget.vaadin.views.financial;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetailFixedMovementViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailFixedMovementPresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());
        when(presenter.findLaunches(null)).thenReturn(List.of());

        var view = new DetailFixedMovementView(presenter);

        assertThat(view.identificationField).isNotNull();
        assertThat(view.valueField).isNotNull();
        assertThat(view.autoLaunchCheckbox).isNotNull();
        assertThat(view.launchesGrid).isNotNull();
    }

    @Test
    void launches_grid_has_columns() {
        var presenter = mock(DetailFixedMovementPresenter.class);
        when(presenter.findById(anyLong())).thenReturn(Optional.empty());
        when(presenter.findLaunches(null)).thenReturn(List.of());

        var view = new DetailFixedMovementView(presenter);

        assertThat(view.launchesGrid.getColumns()).hasSizeGreaterThanOrEqualTo(3);
    }
}
