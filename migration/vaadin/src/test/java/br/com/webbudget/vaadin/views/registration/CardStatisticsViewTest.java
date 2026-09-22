package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CardStatisticsViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(CardStatisticsPresenter.class);
        var view = new CardStatisticsView(presenter);

        assertThat(view.cardNameField).isNotNull();
        assertThat(view.cardNumberField).isNotNull();
        assertThat(view.cardTypeField).isNotNull();
    }

    @Test
    void grids_are_present() {
        var presenter = mock(CardStatisticsPresenter.class);
        var view = new CardStatisticsView(presenter);

        assertThat(view.invoicesGrid).isNotNull();
        assertThat(view.invoicesGrid.getColumns()).hasSize(4);
        assertThat(view.detailedConsumeGrid).isNotNull();
        assertThat(view.detailedConsumeGrid.getColumns()).hasSize(3);
    }

    @Test
    void back_button_is_present() {
        var presenter = mock(CardStatisticsPresenter.class);
        var view = new CardStatisticsView(presenter);

        assertThat(view.backButton).isNotNull();
    }

    @Test
    void fields_are_read_only() {
        var presenter = mock(CardStatisticsPresenter.class);
        var view = new CardStatisticsView(presenter);

        assertThat(view.cardNameField.isReadOnly()).isTrue();
        assertThat(view.cardNumberField.isReadOnly()).isTrue();
        assertThat(view.cardTypeField.isReadOnly()).isTrue();
    }
}
