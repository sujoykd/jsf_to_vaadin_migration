package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.repositories.configuration.ConfigurationRepository;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import br.com.webbudget.domain.services.ConfigurationService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingsViewTest {

    private SettingsView createView() {
        var configurationService = mock(ConfigurationService.class);
        var configurationRepository = mock(ConfigurationRepository.class);
        var movementClassRepository = mock(MovementClassRepository.class);

        when(configurationRepository.findCurrent()).thenReturn(Optional.empty());
        when(movementClassRepository.findAllActive()).thenReturn(List.of());

        return new SettingsView(configurationService, configurationRepository, movementClassRepository);
    }

    @Test
    void view_has_credit_card_class_combobox() {
        var view = createView();

        assertThat(view.creditCardClassField).isNotNull();
    }

    @Test
    void combobox_is_required() {
        var view = createView();

        assertThat(view.creditCardClassField.isRequiredIndicatorVisible()).isTrue();
    }
}
