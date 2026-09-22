package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormClosingViewTest {

    @Test
    void period_combobox_is_present() {
        var presenter = mock(FormClosingPresenter.class);
        when(presenter.loadOpenPeriods()).thenReturn(List.of());

        var view = new FormClosingView(presenter);

        assertThat(view.periodComboBox).isNotNull();
        assertThat(view.periodComboBox.getLabel()).isEqualTo("Financial Period");
    }

    @Test
    void view_has_simulate_and_close_buttons() {
        var presenter = mock(FormClosingPresenter.class);
        when(presenter.loadOpenPeriods()).thenReturn(List.of());

        var view = new FormClosingView(presenter);

        long buttonCount = view.getChildren()
                .flatMap(c -> {
                    if (c instanceof com.vaadin.flow.component.orderedlayout.HorizontalLayout hl) {
                        return hl.getChildren();
                    }
                    return java.util.stream.Stream.empty();
                })
                .filter(c -> c instanceof com.vaadin.flow.component.button.Button)
                .count();

        assertThat(buttonCount).isGreaterThanOrEqualTo(2);
    }
}
