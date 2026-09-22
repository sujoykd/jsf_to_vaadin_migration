package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.configuration.Configuration;
import br.com.webbudget.domain.repositories.configuration.ConfigurationRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfigurationServiceTest {

    @Test
    void save_persists_configuration() {
        var config = new Configuration();
        var repo = mock(ConfigurationRepository.class);
        when(repo.save(config)).thenReturn(config);

        var service = new ConfigurationService(repo);
        var result = service.save(config);

        verify(repo).save(config);
        assertThat(result).isSameAs(config);
    }

    @Test
    void update_refreshes_configuration() {
        var config = new Configuration();
        var repo = mock(ConfigurationRepository.class);
        when(repo.saveAndFlushAndRefresh(config)).thenReturn(config);

        var service = new ConfigurationService(repo);
        var result = service.update(config);

        verify(repo).saveAndFlushAndRefresh(config);
        assertThat(result).isSameAs(config);
    }
}
