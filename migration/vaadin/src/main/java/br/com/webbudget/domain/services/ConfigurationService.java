package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.configuration.Configuration;
import br.com.webbudget.domain.repositories.configuration.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Transactional
    public Configuration save(Configuration configuration) {
        return this.configurationRepository.save(configuration);
    }

    @Transactional
    public Configuration update(Configuration configuration) {
        return this.configurationRepository.saveAndFlushAndRefresh(configuration);
    }
}
